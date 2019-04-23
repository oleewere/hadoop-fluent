package com.cloudera.hadoop.cloud;

import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;
import com.cloudera.hadoop.cloud.output.HadoopOutput;
import com.cloudera.hadoop.cloud.upload.CloudStorageUploader;
import com.cloudera.hadoop.cloud.upload.HDFSUploadClient;
import com.cloudera.hadoop.cloud.upload.UploadClient;
import influent.forward.ForwardCallback;
import influent.forward.ForwardServer;

public class Forward {

  public static void main(String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException("Please provide at least one argument with a valid path (as configuration ini file)");
    }

    final HadoopFluentConf hadoopFluentConf = new HadoopFluentConf(args[0]);

    final UploadClient uploadClient = new HDFSUploadClient();
    uploadClient.init(hadoopFluentConf);

    final CloudStorageUploader uploader = new CloudStorageUploader(uploadClient, hadoopFluentConf);
    uploader.setDaemon(true);
    uploader.setName("cloud-storage-uploader");
    uploader.start();

    final HadoopOutput hadoopOutput = new HadoopOutput(hadoopFluentConf, uploader);

    final ForwardCallback callback = ForwardCallback.of(hadoopOutput::handleEvent);
    final ForwardServer server = new ForwardServer
      .Builder(callback)
      .build();
    server.start();
  }

}
