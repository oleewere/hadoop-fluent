package com.cloudera.hadoop.cloud.conf;

public class UploaderConf {

  private final String basePath;
  private final Integer timeoutMins;
  private final Integer intervalSeconds;
  private final boolean uploadOnShutdown;

  public UploaderConf(String basePath, Integer timeoutMins, Integer intervalSeconds, boolean uploadOnShutdown) {
    this.basePath = basePath;
    this.timeoutMins = timeoutMins;
    this.intervalSeconds = intervalSeconds;
    this.uploadOnShutdown = uploadOnShutdown;
  }

  public String getBasePath() {
    return basePath;
  }

  public Integer getTimeoutMins() {
    return timeoutMins;
  }

  public Integer getIntervalSeconds() {
    return intervalSeconds;
  }

  public boolean isUploadOnShutdown() {
    return uploadOnShutdown;
  }
}
