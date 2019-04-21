package com.cloudera.hadoop.cloud.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class HDFSUtil {

  private static final Logger logger = LogManager.getLogger(HDFSUtil.class);

  public static void copyFromLocal(String sourceFilepath, String destFilePath, FileSystem fileSystem, boolean overwrite,
                                   boolean delSrc) throws Exception {
    String fsUri = fileSystem.getUri().toString();
    Path src = new Path(sourceFilepath);
    Path dst = new Path(destFilePath);
    logger.info("Copying localfile '{}' to hdfsPath (FS base URI: {}) '{}'", sourceFilepath, fsUri, destFilePath);
    fileSystem.copyFromLocalFile(delSrc, overwrite, src, dst);
  }

  public static FileSystem buildFileSystem(String hdfsHost, String hdfsPort) {
    return buildFileSystem(hdfsHost, hdfsPort, "hdfs");
  }

  public static FileSystem buildFileSystem(String hdfsHost, String hdfsPort, String scheme) {
    Configuration configuration = buildHdfsConfiguration(hdfsHost, hdfsPort, scheme);
    return buildFileSystem(configuration);
  }

  public static FileSystem buildFileSystem(Configuration configuration) {
    return buildFileSystem(configuration, 5);
  }

  public static FileSystem buildFileSystem(Configuration configuration, int sleepSeconds) {
    while (true) {
      try {
        return FileSystem.get(configuration);
      } catch (Exception e) {
        logger.error("Exception during buildFileSystem call:", e);
      }
      try {
        Thread.sleep(1000 * sleepSeconds);
      } catch (InterruptedException e) {
        logger.error("Error during thread sleep (filesystem bootstrap)", e);
        Thread.currentThread().interrupt();
        return null;
      }
    }
  }

  public static Configuration buildHdfsConfiguration(String hdfsHost, String hdfsPort, String scheme) {
    return buildHdfsConfiguration(String.format("%s:%s", hdfsHost, hdfsPort), scheme);
  }

  public static Configuration buildHdfsConfiguration(String address, String scheme) {
    String url = String.format("%s://%s/", scheme, address);
    Configuration configuration = new Configuration();
    configuration.set("fs.defaultFS", url);
    return configuration;
  }

  public static void closeFileSystem(FileSystem fileSystem) {
    if (fileSystem != null) {
      try {
        fileSystem.close();
      } catch (IOException e) {
        logger.error(e.getLocalizedMessage(), e.getCause());
      }
    }
  }

  /**
   * Override Hadoop configuration object based on logfeeder.properties configurations (with keys that starts with "fs." or "hadoop.*")
   * @param configMap global property holder
   * @param configuration hadoop configuration holder
   */
  public static void overrideFileSystemConfigs(Map<String, String> configMap, Configuration configuration) {
    for (Map.Entry<String, String> prop : configMap.entrySet()) {
      String propertyName = prop.getKey();
      if (propertyName.startsWith("fs.")) {
        logger.info("Override {} configuration (by logfeeder.properties)", propertyName);
        configuration.set(propertyName, prop.getValue());
      }
    }
  }
}
