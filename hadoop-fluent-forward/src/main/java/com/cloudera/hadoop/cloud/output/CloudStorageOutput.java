package com.cloudera.hadoop.cloud.output;

import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CloudStorageOutput {

  private static final Logger logger = LogManager.getLogger(CloudStorageOutput.class);

  private final HadoopFluentConf hadoopFluentConf;

  public CloudStorageOutput(HadoopFluentConf hadoopFluentConf) {
    this.hadoopFluentConf = hadoopFluentConf;
  }
}
