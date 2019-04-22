package com.cloudera.hadoop.cloud.conf;

public class UploaderConf {

  private final String basePath;
  private final Integer timeoutMins;
  private final Integer intervalSeconds;

  public UploaderConf(String basePath, Integer timeoutMins, Integer intervalSeconds) {
    this.basePath = basePath;
    this.timeoutMins = timeoutMins;
    this.intervalSeconds = intervalSeconds;
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
}
