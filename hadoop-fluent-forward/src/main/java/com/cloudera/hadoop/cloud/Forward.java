package com.cloudera.hadoop.cloud;

import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;
import influent.forward.ForwardCallback;
import influent.forward.ForwardServer;

import java.util.concurrent.CompletableFuture;

public class Forward {

  public static void main(String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException("Please provide at least one argument with a valid path (as configuration ini file)");
    }

    HadoopFluentConf hadoopFluentConf = new HadoopFluentConf(args[0]);

    final ForwardCallback callback = ForwardCallback.of(stream -> CompletableFuture.completedFuture(null));
    final ForwardServer server = new ForwardServer
      .Builder(callback)
      .build();

    server.start();
  }

}
