package com.cloudera.hadoop.cloud.output;

import com.cloudera.hadoop.cloud.buffer.LogFileBufferFactory;
import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;
import influent.EventEntry;
import influent.EventStream;
import influent.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HadoopOutput {

  private static final Logger logger = LogManager.getLogger(HadoopOutput.class);

  private final HadoopFluentConf hadoopFluentConf;
  private final LoggerContext loggerContext;
  private final Map<String, Logger> logOutputs = new HashMap<>();

  public HadoopOutput(HadoopFluentConf hadoopFluentConf) {
    this.hadoopFluentConf = hadoopFluentConf;
    loggerContext = (LoggerContext) LogManager.getContext(false);
  }

  public synchronized CompletableFuture<Void> handleEvent(EventStream stream) {
    Tag tag = stream.getTag();
    String tagName = tag.getName();
    if (!logOutputs.containsKey(tagName)) {
      logger.info("New tag found: {} ... Creating a new logger.", tagName);
      final Logger newLogger = LogFileBufferFactory.createLogger(tagName, loggerContext, hadoopFluentConf);
      logOutputs.put(tagName, newLogger);
    }
    return write(logOutputs.get(tagName), stream.getEntries());
  }

  private CompletableFuture<Void> write(Logger logger, List<EventEntry> entries) {
    for (EventEntry eventEntry : entries) {
      logger.info(eventEntry.getRecord());
    }
    return CompletableFuture.completedFuture(null);
  }
}
