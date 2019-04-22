package com.cloudera.hadoop.cloud;

import com.cloudera.hadoop.cloud.conf.HadoopFluentConf;
import com.cloudera.hadoop.cloud.output.HadoopOutput;
import influent.forward.ForwardCallback;
import influent.forward.ForwardServer;

public class Forward {

  public static void main(String[] args) {
    if (args.length < 1) {
      throw new IllegalArgumentException("Please provide at least one argument with a valid path (as configuration ini file)");
    }

    final HadoopFluentConf hadoopFluentConf = new HadoopFluentConf(args[0]);
    final HadoopOutput hadoopOutput = new HadoopOutput(hadoopFluentConf);

    final ForwardCallback callback = ForwardCallback.of(hadoopOutput::handleEvent);
    final ForwardServer server = new ForwardServer
      .Builder(callback)
      .build();
    server.start();
  }

}
