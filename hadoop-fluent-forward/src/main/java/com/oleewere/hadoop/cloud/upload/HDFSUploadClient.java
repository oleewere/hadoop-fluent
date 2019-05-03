package com.oleewere.hadoop.cloud.upload;

import com.oleewere.hadoop.cloud.conf.HadoopFluentConf;
import com.oleewere.hadoop.cloud.util.HDFSUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

public class HDFSUploadClient implements UploadClient {

  private static final Logger logger = LogManager.getLogger(HDFSUploadClient.class);

  private final AtomicReference<Configuration> configurationRef = new AtomicReference<>();

  @Override
  public void init(final HadoopFluentConf hadoopFluentConf) {
    logger.info("Initialize HDFS Upload Client ...");
    final Configuration configuration = new Configuration();
    HDFSUtil.overrideFileSystemConfigs(hadoopFluentConf, configuration);
    configurationRef.set(configuration);
  }

  @Override
  public void upload(final String source, final String target) throws Exception {
    final FileSystem fs = HDFSUtil.buildFileSystem(configurationRef.get());
    HDFSUtil.copyFromLocal(source, target, fs, true, true);
  }

  @Override
  public void close() {
  }
}
