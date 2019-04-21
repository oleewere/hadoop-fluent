package com.cloudera.hadoop.cloud.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class CloudStorageLoggerConf {

  private static final Logger logger = LogManager.getLogger(CloudStorageLoggerConf.class);

  public static String hostName = null;
  public static String ipAddress = null;

  static {
    try {
      InetAddress ip = InetAddress.getLocalHost();
      ipAddress = ip.getHostAddress();
      String getHostName = ip.getHostName();
      String getCanonicalHostName = ip.getCanonicalHostName();
      if (!getCanonicalHostName.equalsIgnoreCase(ipAddress)) {
        logger.info("Using getCanonicalHostName()=" + getCanonicalHostName);
        hostName = getCanonicalHostName;
      } else {
        logger.info("Using getHostName()=" + getHostName);
        hostName = getHostName;
      }
      logger.info("ipAddress=" + ipAddress + ", getHostName=" + getHostName + ", getCanonicalHostName=" + getCanonicalHostName +
        ", hostName=" + hostName);
    } catch (UnknownHostException e) {
      logger.error("Error getting hostname.", e);
    }
  }

  private final Map<String, Object> configMap = new HashMap<>();

  public String getClusterName() {
    return null;
  }

  public String getRolloverArchiveBaseDir() {
    return null;
  }

  public boolean isUseGzip() {
    return false;
  }

  public Integer getRolloverSize() {
    return null;
  }

  public String getRolloverSizeFormat() {
    return null;
  }

  public Integer getRolloverThresholdTimeMins() {
    return null;
  }

  public boolean isRolloverOnStartup() {
    return false;
  }

  public Integer getRolloverMaxBackupFiles() {
    return null;
  }

  public boolean isImmediateFlush() {
    return false;
  }
}
