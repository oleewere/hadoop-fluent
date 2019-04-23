package com.cloudera.hadoop.cloud.conf;

public class BufferConf {

  private final String rolloverSizeFormat;
  private final String rolloverArchiveBaseDir;
  private final Integer rolloverSize;
  private final Integer rolloverThresholdTimeMins;
  private final Integer rolloverMaxBackupFiles;
  private final boolean useGzip;
  private final boolean rolloverOnStartup;
  private final boolean rolloverOnShutdown;
  private final boolean immediateFlush;
  private final boolean asyncLogProcess;

  private BufferConf(Builder builder) {
    rolloverSizeFormat = builder.rolloverSizeFormat;
    rolloverArchiveBaseDir = builder.rolloverArchiveBaseDir;
    rolloverSize = builder.rolloverSize;
    rolloverThresholdTimeMins = builder.rolloverThresholdTimeMins;
    rolloverMaxBackupFiles = builder.rolloverMaxBackupFiles;
    useGzip = builder.useGzip;
    rolloverOnStartup = builder.rolloverOnStartup;
    rolloverOnShutdown = builder.rolloverOnShutdown;
    immediateFlush = builder.immediateFlush;
    asyncLogProcess = builder.asyncLogProcess;
  }

  public String getRolloverArchiveBaseDir() {
    return this.rolloverArchiveBaseDir;
  }

  public boolean isUseGzip() {
    return this.useGzip;
  }

  public Integer getRolloverSize() {
    return this.rolloverSize;
  }

  public String getRolloverSizeFormat() {
    return this.rolloverSizeFormat;
  }

  public Integer getRolloverThresholdTimeMins() {
    return this.rolloverThresholdTimeMins;
  }

  public boolean isRolloverOnStartup() {
    return this.rolloverOnStartup;
  }

  public boolean isRolloverOnShutdown() {
    return rolloverOnShutdown;
  }

  public Integer getRolloverMaxBackupFiles() {
    return this.rolloverMaxBackupFiles;
  }

  public boolean isImmediateFlush() {
    return this.immediateFlush;
  }

  public boolean isAsyncLogProcess() {
    return asyncLogProcess;
  }

  public static class Builder {
    private String rolloverSizeFormat;
    private String rolloverArchiveBaseDir;
    private Integer rolloverSize;
    private Integer rolloverThresholdTimeMins;
    private Integer rolloverMaxBackupFiles;
    private boolean useGzip;
    private boolean rolloverOnStartup;
    private boolean rolloverOnShutdown;
    private boolean immediateFlush;
    private boolean asyncLogProcess;

    public Builder withRolloverSizeFormat(String rolloverSizeFormat){
      this.rolloverSizeFormat = rolloverSizeFormat;
      return this;
    }

    public Builder withRolloverArchiveBaseDir(String rolloverArchiveBaseDir){
      this.rolloverArchiveBaseDir = rolloverArchiveBaseDir;
      return this;
    }

    public Builder withRolloverSize(Integer rolloverSize){
      this.rolloverSize = rolloverSize;
      return this;
    }

    public Builder withRolloverThresholdTimeMins(Integer rolloverThresholdTimeMins){
      this.rolloverThresholdTimeMins = rolloverThresholdTimeMins;
      return this;
    }

    public Builder withRolloverMaxBackupFiles(Integer rolloverMaxBackupFiles){
      this.rolloverMaxBackupFiles = rolloverMaxBackupFiles;
      return this;
    }

    public Builder withUseGzip(boolean useGzip){
      this.useGzip = useGzip;
      return this;
    }

    public Builder withRolloverOnStartup(boolean rolloverOnStartup){
      this.rolloverOnStartup = rolloverOnStartup;
      return this;
    }

    public Builder withRolloverOnShutdown(boolean rolloverOnShutdown){
      this.rolloverOnShutdown = rolloverOnShutdown;
      return this;
    }

    public Builder withImmediateFlush(boolean immediateFlush){
      this.immediateFlush = immediateFlush;
      return this;
    }

    public Builder withAsyncLogProcess(boolean asyncLogProcess) {
      this.asyncLogProcess = asyncLogProcess;
      return this;
    }

    public BufferConf build() {
      return new BufferConf(this);
    }
  }
}
