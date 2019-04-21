package com.cloudera.hadoop.cloud.output;

import com.cloudera.hadoop.cloud.logger.CloudStorageLoggerConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CloudStorageOutput {

  private static final Logger logger = LogManager.getLogger(CloudStorageOutput.class);

  private final CloudStorageLoggerConf cloudStorageLoggerConf;

  public CloudStorageOutput() {
    cloudStorageLoggerConf = null;
  }
}
