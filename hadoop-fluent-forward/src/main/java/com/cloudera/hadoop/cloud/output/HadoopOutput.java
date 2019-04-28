package com.cloudera.hadoop.cloud.output;

import com.cloudera.hadoop.cloud.buffer.LogFileBufferFactory;
import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;
import com.cloudera.hadoop.cloud.upload.HadoopFileUploader;
import influent.EventEntry;
import influent.EventStream;
import influent.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HadoopOutput {

  private static final Logger logger = LogManager.getLogger(HadoopOutput.class);

  private final HadoopFluentConf hadoopFluentConf;
  private final HadoopFileUploader uploader;
  private final LoggerContext loggerContext;
  private final Map<String, Logger> logOutputs = new HashMap<>();

  public HadoopOutput(HadoopFluentConf hadoopFluentConf, HadoopFileUploader uploader) {
    this.hadoopFluentConf = hadoopFluentConf;
    this.uploader = uploader;
    loggerContext = (LoggerContext) LogManager.getContext(false);
    addJvmShutdownHook();
  }

  public CompletableFuture<Void> handleEvent(EventStream stream) {
    Tag tag = stream.getTag();
    String tagName = tag.getName();
    synchronized(this) {
      if (!logOutputs.containsKey(tagName)) {
        logger.info("New tag found: {} ... Creating a new logger.", tagName);
        final Logger newLogger = LogFileBufferFactory.createLogger(tagName, loggerContext, hadoopFluentConf);
        logOutputs.put(tagName, newLogger);
      }
    }
    if (hadoopFluentConf.getBufferConf().isAsyncLogProcess()) {
      return CompletableFuture.runAsync(() -> {
        write(logOutputs.get(tagName), stream.getEntries());
      });
    } else {
      write(logOutputs.get(tagName), stream.getEntries());
      return CompletableFuture.completedFuture(null);
    }
  }

  private void write(final Logger logger, final List<EventEntry> entries) {
    for (EventEntry eventEntry : entries) {
      logger.info(eventEntry.getRecord());
    }
  }

  private void removeLoggers() {
    for (Map.Entry<String, Logger> loggerEntry : logOutputs.entrySet()) {
      removeLogger(loggerEntry.getKey());
    }
  }

  private void removeLogger(String tag) {
    if (hadoopFluentConf.getBufferConf().isRolloverOnShutdown() && logOutputs.containsKey(tag)) {
      rollover(logOutputs.get(tag));
    }
    logger.info("Remove logger by tag: {}", tag);
    Configuration config = loggerContext.getConfiguration();
    config.removeLogger(tag);
    loggerContext.updateLoggers();
    logOutputs.remove(tag);
  }

  private void rollover(Logger logger) {
    Map<String, Appender> appenders = ((org.apache.logging.log4j.core.Logger) logger).getAppenders();
    for (Map.Entry<String, Appender> stringAppenderEntry : appenders.entrySet()) {
      Appender appender = stringAppenderEntry.getValue();
      if (appender instanceof RollingFileAppender) {
        ((RollingFileAppender) appender).getManager().rollover();
      }
    }
  }

  private void addJvmShutdownHook() {
    boolean uploadOnShutdow = hadoopFluentConf.getUploaderConf().isUploadOnShutdown();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Running JVM shutdown hook ...");
      this.removeLoggers();
      if (uploadOnShutdow) {
        uploader.interrupt();
        uploader.doUpload(2);
      }
    }));
  }
}
