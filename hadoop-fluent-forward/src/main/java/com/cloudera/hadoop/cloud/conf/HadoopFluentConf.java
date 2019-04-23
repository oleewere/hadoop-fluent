package com.cloudera.hadoop.cloud.conf;

import com.cloudera.hadoop.cloud.util.IniUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HadoopFluentConf {
  private static final Logger logger = LogManager.getLogger(HadoopFluentConf.class);

  public static String hostName = null;
  public static String ipAddress = null;

  static {
    try {
      InetAddress ip = InetAddress.getLocalHost();
      ipAddress = ip.getHostAddress();
      String getHostName = ip.getHostName();
      String getCanonicalHostName = ip.getCanonicalHostName();
      if (!getCanonicalHostName.equalsIgnoreCase(ipAddress)) {
        logger.info("Use canonical hostname: " + getCanonicalHostName);
        hostName = getCanonicalHostName;
      } else {
        logger.info("Use hostname: " + getHostName);
        hostName = getHostName;
      }
      logger.info("IP Address; " + ipAddress + ", Hostname: " + getHostName + ", Canonical Hostname: " + getCanonicalHostName);
    } catch (UnknownHostException e) {
      logger.error("Error getting hostname.", e);
    }
  }

  private final Map<String, Properties> configs = new HashMap<>();
  private final BufferConf bufferConf;
  private final UploaderConf uploaderConf;
  private final String clusterName;
  private final Integer port;

  public HadoopFluentConf(String iniFilePath) {
    try (final FileReader reader = new FileReader(iniFilePath)) {
      IniUtil.parseIni(reader, configs);
    } catch (Exception e){
      logger.error("Error occurred during reading ini configuration file.", e);
      System.exit(1);
    }
    this.clusterName = getConfigAsString("server", "cluster", "cl1");
    this.port = getConfigAsInteger("server", "forward_port", 24224);
    this.bufferConf = new BufferConf.Builder()
      .withRolloverArchiveBaseDir(getConfigAsString("buffer", "rollover_archive_base_dir", "/var/lib/hadoop-fluent/data"))
      .withRolloverSize(getConfigAsInteger("buffer", "rollover_size", 256))
      .withRolloverSizeFormat(getConfigAsString("buffer", "rollover_size_format", "MB"))
      .withRolloverThresholdTimeMins(getConfigAsInteger("buffer", "rollover_threshold_time_mins", 5))
      .withImmediateFlush(getConfigAsBoolean("buffer", "immediate_flush", true))
      .withRolloverMaxBackupFiles(getConfigAsInteger("buffer", "rollover_max_backup_files", 10))
      .withRolloverOnShutdown(getConfigAsBoolean("buffer", "rollover_on_shutdown", true))
      .withRolloverOnStartup(getConfigAsBoolean("buffer", "rollover_on_startup", true))
      .withUseGzip(getConfigAsBoolean("buffer", "use_gzip", true))
      .withAsyncLogProcess(getConfigAsBoolean("buffer", "async_log_process", false))
      .build();
    this.uploaderConf = new UploaderConf(
      "",
      getConfigAsInteger("uploader", "timeout_mins", 10),
      getConfigAsInteger("uploader", "interval_secs", 300),
      getConfigAsBoolean("uploader", "upload_on_shutdown", true)
      );
  }

  public BufferConf getBufferConf() {
    return bufferConf;
  }

  public UploaderConf getUploaderConf() {
    return uploaderConf;
  }

  public String getClusterName() {
    return clusterName;
  }

  public Integer getPort() {
    return port;
  }

  public String getConfigAsString(String type, String key, String defaultValue) {
    if (configs.containsKey(type) && configs.get(type).containsKey(key)) {
      return configs.get(type).getProperty(key);
    }
    return defaultValue;
  }

  public int getConfigAsInteger(String type, String key, Integer defaultValue) {
    if (configs.containsKey(type) && configs.get(type).containsKey(key)) {
      return Integer.parseInt(configs.get(type).getProperty(key));
    }
    return defaultValue;
  }

  public boolean getConfigAsBoolean(String type, String key, Boolean defaultValue) {
    if (configs.containsKey(type) && configs.get(type).containsKey(key)) {
      return Boolean.parseBoolean(configs.get(type).getProperty(key));
    }
    return defaultValue;
  }
}
