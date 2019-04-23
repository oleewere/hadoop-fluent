package com.cloudera.hadoop.cloud.upload;

import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CloudStorageUploader extends Thread {

  private static final Logger logger = LogManager.getLogger(CloudStorageUploader.class);

  private final UploadClient uploadClient;
  private final HadoopFluentConf hadoopFluentConf;
  private final ExecutorService executorService;

  public CloudStorageUploader(final UploadClient uploadClient, final HadoopFluentConf hadoopFluentConf) {
    this.uploadClient = uploadClient;
    this.hadoopFluentConf = hadoopFluentConf;
    this.executorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public void run() {
    logger.info("Cloud storage uploader started.");
    boolean stop = false;
    do {
      try {
        try {
          doUpload(hadoopFluentConf.getUploaderConf().getTimeoutMins());
        } catch (Exception e) {
          logger.error("An error occurred during Uploader operation.", e);
        }
        Thread.sleep(1000 * hadoopFluentConf.getUploaderConf().getIntervalSeconds());
      } catch (InterruptedException ie) {
        logger.info("Cloud storage uploader thread interrupted");
        stop = true;
      }
    } while (!stop && !Thread.currentThread().isInterrupted());
  }

  /**
   * Finds .log and .gz files and upload them to cloud storage by an uploader client
   */
  public void doUpload(int timeout) {
    try {
      final File archiveLogDir = Paths.get(
        hadoopFluentConf.getBufferConf().getRolloverArchiveBaseDir(), hadoopFluentConf.getClusterType(), hadoopFluentConf.getClusterName(), HadoopFluentConf.hostName, "archived").toFile();
      if (archiveLogDir.exists()) {
        String[] extensions = {"log", "json", "gz"};
        Collection<File> filesToUpload = FileUtils.listFiles(archiveLogDir, extensions, true);
        if (filesToUpload.isEmpty()) {
          logger.debug("Not found any files to upload.");
        } else {
          for (File file : filesToUpload) {
            final String outputPath = generateOutputPath(hadoopFluentConf.getUploaderConf().getBasePath(),
              hadoopFluentConf.getClusterType(), hadoopFluentConf.getClusterName(), HadoopFluentConf.hostName, file);
            logger.info("Upload will start: input: {}, output: {}", file.getAbsolutePath(), outputPath);
            Future<?> future = executorService.submit(() -> {
              try {
                uploadClient.upload(file.getAbsolutePath(), outputPath);
              } catch (InterruptedException ie) {
                logger.error("Cloud upload thread interrupted", ie);
              } catch (Exception e) {
                logger.error("Exception during cloud upload", e);
              }
            });
            future.get(timeout, TimeUnit.MINUTES);
          }
        }
      } else {
        logger.debug("Directory {} does not exist.", archiveLogDir);
      }
    } catch (Exception e) {
      logger.error("Exception during cloud upload", e);
    }
  }

  @VisibleForTesting
  String generateOutputPath(String basePath, String clusterType, String clusterName, String hostName, File localFile) {
    final String outputWithoutBasePath = Paths.get(clusterType, clusterName, hostName,
      localFile.getParentFile().getName(), localFile.getName()).toString();
    final String outputPath;
    if (StringUtils.isNotEmpty(basePath)) {
      if (!basePath.endsWith("/")){
        outputPath = basePath + "/" + outputWithoutBasePath;
      } else {
        outputPath = basePath + outputWithoutBasePath;
      }
    } else {
      outputPath = outputWithoutBasePath;
    }
    return outputPath;
  }
}
