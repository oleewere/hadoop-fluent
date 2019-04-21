package com.cloudera.hadoop.cloud.upload;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CloudStorageUploader {

  private static final Logger logger = LogManager.getLogger(CloudStorageUploader.class);

  private final UploadClient uploadClient;

  public CloudStorageUploader(final UploadClient uploadClient) {
    this.uploadClient = uploadClient;
  }
}
