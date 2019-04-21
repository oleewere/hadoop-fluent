package com.cloudera.hadoop.cloud;

import java.io.Closeable;
import java.util.Map;

public interface UploadClient extends Closeable {

  /**
   * Initialize the client
   * @param configs key/value map that holds configurations for the uploader client
   */
  void init(Map<String, String> configs);

  /**
   * Upload source file to cloud storage location
   * @param source file that will be uploaded
   * @param target file key/output on cloud storage
   * @throws Exception error during upload
   */
  void upload(String source, String target) throws Exception;
}
