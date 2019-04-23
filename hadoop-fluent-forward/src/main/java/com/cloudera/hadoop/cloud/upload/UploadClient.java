package com.cloudera.hadoop.cloud.upload;

import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;

import java.io.Closeable;

public interface UploadClient extends Closeable {

  /**
   * Initialize the client
   * @param hadoopFluentConf holds configurations for the uploader client
   */
  void init(HadoopFluentConf hadoopFluentConf);

  /**
   * Upload source file to cloud storage location
   * @param source file that will be uploaded
   * @param target file key/output on cloud storage
   * @throws Exception error during upload
   */
  void upload(String source, String target) throws Exception;
}
