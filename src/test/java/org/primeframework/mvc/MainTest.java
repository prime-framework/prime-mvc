/*
 * Copyright (c) 2021, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.primeframework.mvc;

import sun.misc.Signal;

public class MainTest {
  public static void main(String[] args) {
    Signal.handle(new Signal("INT"), signal -> {
      System.out.println("Interrupted and shutting down gracefully");
      System.exit(0);
    });
//    var main = Thread.currentThread();
//    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//      System.out.println("Shutting down in hook");
//      main.interrupt();
//      System.out.println("interrupted");
//      try {
//        main.join();
////        Thread.sleep(10_000);
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      }
//    }));
//
    System.out.println("Starting");
    try {
      Thread.sleep(1_000_000_000);
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.out.println("Caught");
    } finally {
      System.out.println("Finally");
    }

    System.out.println("Exiting");
  }
}
