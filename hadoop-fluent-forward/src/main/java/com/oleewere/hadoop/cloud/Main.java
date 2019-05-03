package com.oleewere.hadoop.cloud;

import com.oleewere.hadoop.cloud.conf.HadoopFluentConf;
import com.oleewere.hadoop.cloud.output.HadoopOutput;
import com.oleewere.hadoop.cloud.upload.HadoopFileUploader;
import com.oleewere.hadoop.cloud.upload.HDFSUploadClient;
import com.oleewere.hadoop.cloud.upload.UploadClient;
import influent.forward.ForwardCallback;
import influent.forward.ForwardServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

  private static final Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException("Please provide at least one argument with a valid path (as configuration ini file)");
    }

    final HadoopFluentConf hadoopFluentConf = new HadoopFluentConf(args[0]);
    boolean serverMode = "server".equalsIgnoreCase(hadoopFluentConf.getMode());

    logger.info("Staring hadoop-fluent in '{}' mode.", serverMode ? "server" : "uploader");

    final UploadClient uploadClient = new HDFSUploadClient();
    uploadClient.init(hadoopFluentConf);

    final HadoopFileUploader uploader = new HadoopFileUploader(uploadClient, hadoopFluentConf);
    if (serverMode) {
      uploader.setDaemon(true);
    }
    uploader.setName("hadoop-file-uploader");
    uploader.start();

    if (serverMode) {
      final HadoopOutput hadoopOutput = new HadoopOutput(hadoopFluentConf, uploader);
      final ForwardCallback callback = ForwardCallback.of(hadoopOutput::handleEvent);
      final ForwardServer server = new ForwardServer
        .Builder(callback)
        .localAddress(hadoopFluentConf.getPort())
        .build();
      server.start();
    }
  }
}
