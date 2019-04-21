package com.cloudera.hadoop.cloud;

import influent.forward.ForwardCallback;
import influent.forward.ForwardServer;

import java.util.concurrent.CompletableFuture;

public class Forward {

  public static void main(String[] args) {
    final ForwardCallback callback = ForwardCallback.of(stream -> {
      return CompletableFuture.completedFuture(null);
    });
    final ForwardServer server = new ForwardServer
      .Builder(callback)
      .build();

    server.start();
  }

}
