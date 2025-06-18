package com.daham.client.grpc;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@QuarkusMain
public class GrpcClientMain implements QuarkusApplication {

  public static void main(String... args) {
    Quarkus.run(GrpcClientMain.class, args);
  }

  @Override
  public int run(String... args) {
    try(var beehiveSimulator1 = new BeehiveSimulator("/beehives/beehive1.properties");
        var beehiveSimulator2 = new BeehiveSimulator("/beehives/beehive2.properties");
        var beehiveSimulator3 = new BeehiveSimulator("/beehives/beehive3.properties")) {
      beehiveSimulator1.start();
      beehiveSimulator2.start();
      beehiveSimulator3.start();

      Quarkus.waitForExit();
    } catch (Exception e) {
      return 1;
    }
    return 0;
  }
}
