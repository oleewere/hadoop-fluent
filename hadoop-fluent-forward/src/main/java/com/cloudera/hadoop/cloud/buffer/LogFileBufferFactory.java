package com.cloudera.hadoop.cloud.buffer;

import com.cloudera.hadoop.cloud.conf.BufferConf;
import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.nio.file.Paths;

public class LogFileBufferFactory {
  private static final String ACTIVE_FOLDER = "active";
  private static final String ARCHIVED_FOLDER = "archived";
  private static final String DATE_PATTERN_SUFFIX_GZ = "-%d{yyyy-MM-dd-HH-mm-ss-SSS}.log.gz";
  private static final String DATE_PATTERN_SUFFIX = "-%d{yyyy-MM-dd-HH-mm-ss-SSS}.log";

  public static Logger createLogger(String tag, LoggerContext loggerContext, HadoopFluentConf fluentConf) {
    Configuration config = loggerContext.getConfiguration();
    BufferConf conf = fluentConf.getBufferConf();
    String baseDir = conf.getRolloverArchiveBaseDir();
    String clusterHostnameBaseDir = Paths.get(baseDir, fluentConf.getClusterType(), fluentConf.getClusterName(),
      HadoopFluentConf.hostName).toFile().getAbsolutePath();
    String activeLogDir = Paths.get(clusterHostnameBaseDir, ACTIVE_FOLDER, tag).toFile().getAbsolutePath();
    String archiveLogDir = Paths.get(clusterHostnameBaseDir, ARCHIVED_FOLDER, tag).toFile().getAbsolutePath();

    boolean useGzip = conf.isUseGzip();
    final String archiveFilePattern = useGzip ? DATE_PATTERN_SUFFIX_GZ : DATE_PATTERN_SUFFIX;

    String logSuffix = ".log";
    String fileName = String.join(File.separator, activeLogDir, tag + logSuffix);
    String filePattern = String.join(File.separator, archiveLogDir, tag + archiveFilePattern);
    PatternLayout layout = PatternLayout.newBuilder()
      .withPattern(PatternLayout.DEFAULT_CONVERSION_PATTERN).build();

    String rolloverSize = conf.getRolloverSize().toString() + conf.getRolloverSizeFormat();
    SizeBasedTriggeringPolicy sizeBasedTriggeringPolicy = SizeBasedTriggeringPolicy.createPolicy(rolloverSize);

    final int thresholdMin = conf.getRolloverThresholdTimeMins();
    final int thresholdInterval = thresholdMin * 60000; // 1 min = 60000 milliseconds

    TimeBasedTriggeringPolicy timeBasedTriggeringPolicy = TimeBasedTriggeringPolicy.newBuilder()
      .withInterval(thresholdInterval)
      .build();

    final CompositeTriggeringPolicy compositeTriggeringPolicy;

    if (conf.isRolloverOnStartup()) {
      OnStartupTriggeringPolicy onStartupTriggeringPolicy = OnStartupTriggeringPolicy.createPolicy(1);
      compositeTriggeringPolicy = CompositeTriggeringPolicy
        .createPolicy(sizeBasedTriggeringPolicy, timeBasedTriggeringPolicy, onStartupTriggeringPolicy);
    } else {
      compositeTriggeringPolicy = CompositeTriggeringPolicy
        .createPolicy(sizeBasedTriggeringPolicy, timeBasedTriggeringPolicy);
    }

    DefaultRolloverStrategy defaultRolloverStrategy = DefaultRolloverStrategy.newBuilder()
      .withMax(String.valueOf(conf.getRolloverMaxBackupFiles()))
      .withConfig(config)
      .build();

    boolean immediateFlush = conf.isImmediateFlush();
    RollingFileAppender appender = RollingFileAppender.newBuilder()
      .withFileName(fileName)
      .withFilePattern(filePattern)
      .withLayout(layout)
      .withName(tag)
      .withPolicy(compositeTriggeringPolicy)
      .withStrategy(defaultRolloverStrategy)
      .withImmediateFlush(immediateFlush)
      .build();

    appender.start();
    config.addAppender(appender);

    AppenderRef ref = AppenderRef.createAppenderRef(tag, null, null);
    AppenderRef[] refs = new AppenderRef[] {ref};

    LoggerConfig loggerConfig = LoggerConfig
      .createLogger(false, Level.ALL, tag,
        "true", refs, null, config, null);
    loggerConfig.addAppender(appender, null, null);
    config.addLogger(tag, loggerConfig);
    loggerContext.updateLoggers();
    return loggerContext.getLogger(tag);
  }
}
