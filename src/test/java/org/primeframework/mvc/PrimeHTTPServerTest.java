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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import org.example.action.PostAction;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PrimeHTTPServerTest extends PrimeBaseTest {
  @Test(enabled = false)
  public void load() {
    long start = System.currentTimeMillis();
//    HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.NEVER).priority(256).build();
    for (int i = 0; i < 100_000; i++) {
//      HttpResponse<String> response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:8080/post")).POST(BodyPublishers.noBody()).build(), BodyHandlers.ofString(StandardCharsets.UTF_8));
//      assertEquals(response.statusCode(), 200);
//      assertTrue(response.body().contains("Brian Pontarelli"));
      var response = new RESTClient<>(String.class, String.class)
          .url("http://localhost:" + simulator.getPort() + "/post")
          .successResponseHandler(new TextResponseHandler())
          .errorResponseHandler(new TextResponseHandler())
          .post()
          .go();
      assertEquals(response.status, 200);
      assertTrue(response.successResponse.contains("Brian Pontarelli"));
    }

    long end = System.currentTimeMillis();
    System.out.println("100,000 took [" + (end - start) + "]");
  }

  @Test
  public void post() throws Exception {
    PostAction.invoked = false;
    HttpResponse<String> response = HttpClient.newHttpClient()
                                              .send(HttpRequest.newBuilder()
                                                               .uri(URI.create("http://localhost:" + simulator.port + "/post"))
                                                               .POST(BodyPublishers.noBody())
                                                               .build(),
                                                    info -> BodySubscribers.ofString(StandardCharsets.UTF_8)
                                              );
    assertEquals(response.statusCode(), 200);
    assertTrue(response.body().contains("Brian Pontarelli"));
    assertTrue(PostAction.invoked);
  }

  @Test(enabled = false)
  public void threads() throws Exception {
    long start = System.currentTimeMillis();

    List<Thread> threads = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      final String name = "Thread " + i;
      Thread thread = new Thread(() -> {
        for (int j = 0; j < 10_000; j++) {
          var response = new RESTClient<>(String.class, String.class)
              .url("http://localhost:" + simulator.getPort() + "/echo")
              .addURLParameter("message", name)
              .successResponseHandler(new TextResponseHandler())
              .errorResponseHandler(new TextResponseHandler())
              .get()
              .go();
          assertEquals(response.status, 200);
          assertTrue(response.successResponse.contains(name));
        }
      }, name);
      threads.add(thread);
      thread.start();
    }

    for (int i = 0; i < 10; i++) {
      threads.get(i).join();
    }

    long end = System.currentTimeMillis();
    System.out.println("100,000 threaded took [" + (end - start) + "]");
  }
}
