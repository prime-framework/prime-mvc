/*
 * Copyright (c) 2022, Inversoft Inc., All Rights Reserved
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

import java.nio.file.Path;
import java.util.ArrayList;

import org.example.action.patch.PatchActionRequest.Address;
import org.example.action.patch.PatchActionRequest.CoolObject;
import org.example.action.patch.TestAction;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Daniel DeGroff
 */
public class JSONPatchTest extends PrimeBaseTest {
  private Path jsonDir;

  @BeforeClass
  public void beforeClass() {
    jsonDir = Path.of("src/test/resources/json");
  }

  @BeforeMethod
  public void beforeMethod() {
    super.beforeMethod();

    // Reset
    TestAction.db = null;
  }

  @Test
  public void json_patch_rfc6902() throws Exception {
    // JSON Patch RFC 6902
    // https://www.rfc-editor.org/rfc/rfc6902
    // application/json-patch+json

    // JSON Pointer path:
    // https://www.rfc-editor.org/rfc/rfc6901

    // Initial state
    TestAction.db = new CoolObject();
    TestAction.db.email = "jim@example.com";
    TestAction.db.name = "Jim Bob";
    TestAction.db.addresses = new ArrayList<>();

    // Patch not enabled
    test.simulate(() -> simulator.test("/patch/test-disabled")
                                 .withContentType("application/json-patch+json")
                                 .withBody("""
                                     [
                                       {
                                         "op": "replace",
                                         "path": "/data/email",
                                         "value": "robotdan@fusionauth.io"
                                       }
                                     ]
                                     """)
                                 .patch()
                                 .assertStatusCode(400)
                                 .assertJSON("""
                                     {
                                       "fieldErrors" : { },
                                       "generalErrors" : [ {
                                         "code" : "[PatchNotSupported]",
                                         "message" : "The [Content-Type] HTTP request header value of [application/json-patch+json] is not supported for this request."
                                       } ]
                                     }
                                      """));

    // Change email
    test.simulate(() -> simulator.test("/patch/test")
                                 .withContentType("application/json-patch+json")
                                 .withBody("""
                                     [
                                       {
                                         "op": "replace",
                                         "path": "/data/email",
                                         "value": "robotdan@fusionauth.io"
                                       }
                                     ]
                                     """)
                                 .patch()
                                 .assertStatusCode(200)
                                 .assertJSON("""
                                     {
                                       "data" : {
                                         "addresses" : [ ],
                                         "email" : "robotdan@fusionauth.io",
                                         "name" : "Jim Bob"
                                       }
                                     }
                                      """))

        // Remove email
        .simulate(() -> simulator.test("/patch/test")
                                 .withContentType("application/json-patch+json")
                                 .withBody("""
                                     [
                                       {
                                         "op": "remove",
                                         "path": "/data/email"
                                       }
                                     ]
                                     """)
                                 .patch()
                                 .assertStatusCode(200)
                                 .assertJSON("""
                                     {
                                       "data" : {
                                         "addresses" : [ ],
                                         "name" : "Jim Bob"
                                       }
                                     }
                                      """))

        // Add email
        .simulate(() -> simulator.test("/patch/test")
                                 .withContentType("application/json-patch+json")
                                 .withBody("""
                                     [
                                       {
                                         "op": "add",
                                         "path": "/data/email",
                                         "value": "robotdan@fusionauth.io"
                                       }
                                     ]
                                     """)
                                 .patch()
                                 .assertStatusCode(200)
                                 .assertJSON("""
                                      {
                                       "data" : {
                                         "addresses" : [ ],
                                         "email" : "robotdan@fusionauth.io",
                                         "name" : "Jim Bob"
                                       }
                                     }
                                      """));
  }

  @Test(enabled = false)
  public void json_patch_rfc6902_SCIM_patch() throws Exception {
    // SCIM Patch
    // https://www.rfc-editor.org/rfc/rfc7644#section-3.5.2
    // The general form of the SCIM PATCH request is based on JSON Patch
    //   [RFC6902].  One difference between SCIM PATCH and JSON Patch is that
    //   SCIM servers do not support array indexing and do not support
    //   [RFC6902] operation types relating to array element manipulation,
    //   such as "move".
    //
    // It says it is the same, but the path value for SCIM seems to be unique in that it supports
    // sub attribute matching such as "path":"addresses[type eq \"work\"]" which does not seem to be compliant with
    // RFC 6902

    // Initial state
    TestAction.db = new CoolObject();
    TestAction.db.email = "jim@example.com";
    TestAction.db.name = "Jim Bob";
    TestAction.db.addresses = new ArrayList<>();

    TestAction.db.addresses.add(new Address());
    TestAction.db.addresses.get(0).city = "Denver";
    TestAction.db.addresses.get(0).state = "CO";
    TestAction.db.addresses.get(0).zipCode = 80202;
    TestAction.db.addresses.get(0).type = "work";

    // Change city in nested address
    test.simulate(() -> simulator.test("/patch/test")
                                 .withContentType("application/json-patch+json")
                                 .withBody("""
                                     [
                                       {
                                         "op": "replace",
                                         "path": "/data/addresses[type eq \\"work\\"].city",
                                         "value": "Broomfield"
                                       },
                                       {
                                         "op": "replace",
                                         "path": "/data/addresses[type eq \\"work\\"].zipcode",
                                         "value": 80021
                                       }
                                     ]
                                     """)
                                 .patch()
                                 .assertStatusCode(200)
                                 .assertJSON("""
                                     {
                                       "data" : {
                                         "addresses" : [ {
                                           "city" : "Broomfield",
                                           "state" : "CO",
                                           "type" : "work",
                                           "zipCode" : 80021
                                         } ],
                                         "email" : "robotdan@fusionauth.io",
                                         "name" : "Jim Bob"
                                       }
                                     }
                                      """));
  }

  @Test
  public void json_patch_rfc7386() throws Exception {
    // JSON Merge Patch RFC 7386
    // https://www.rfc-editor.org/rfc/rfc7398
    // https://www.rfc-editor.org/rfc/rfc7396
    // application/merge-patch+json

    // Initial state
    TestAction.db = new CoolObject();
    TestAction.db.email = "jim@example.com";
    TestAction.db.name = "Jim Bob";
    TestAction.db.addresses = new ArrayList<>();

    // Patch not enabled
    test.simulate(() -> simulator.test("/patch/test-disabled")
                                 .withContentType("application/merge-patch+json")
                                 .withBody("""
                                     [
                                       {
                                         "op": "replace",
                                         "path": "/data/email",
                                         "value": "robotdan@fusionauth.io"
                                       }
                                     ]
                                     """)
                                 .patch()
                                 .assertStatusCode(400)
                                 .assertJSON("""
                                     {
                                       "fieldErrors" : { },
                                       "generalErrors" : [ {
                                         "code" : "[PatchNotSupported]",
                                         "message" : "The [Content-Type] HTTP request header value of [application/merge-patch+json] is not supported for this request."
                                       } ]
                                     }
                                      """));

    // Change email
    test.simulate(() -> simulator.test("/patch/test")
                                 .withContentType("application/merge-patch+json")
                                 .withBody("""
                                     {
                                        "data" : {
                                          "email" : "robotdan@fusionauth.io"
                                        }
                                      }
                                      """)
                                 .patch()
                                 .assertStatusCode(200)
                                 .assertJSON("""
                                     {
                                       "data" : {
                                         "addresses" : [ ],
                                         "email" : "robotdan@fusionauth.io",
                                         "name" : "Jim Bob"
                                       }
                                     }
                                      """))

        // Remove email
        .simulate(() -> simulator.test("/patch/test")
                                 .withContentType("application/merge-patch+json")
                                 .withBody("""
                                       {
                                         "data" : {
                                           "email" : null
                                         }
                                       }
                                     """)
                                 .patch()
                                 .assertStatusCode(200)
                                 .assertJSON("""
                                     {
                                       "data" : {
                                         "addresses" : [ ],
                                         "name" : "Jim Bob"
                                       }
                                     }
                                      """))

        // Add email
        .simulate(() -> simulator.test("/patch/test")
                                 .withContentType("application/merge-patch+json")
                                 .withBody("""
                                     {
                                        "data" : {
                                          "email" : "robotdan@fusionauth.io"
                                        }
                                      }
                                      """)
                                 .patch()
                                 .assertStatusCode(200)
                                 .assertJSON("""
                                      {
                                       "data" : {
                                         "addresses" : [ ],
                                         "email" : "robotdan@fusionauth.io",
                                         "name" : "Jim Bob"
                                       }
                                     }
                                      """));
  }

  @Test(dataProvider = "methodOverrides")
  public void patch_MethodOverride(String overrideHeaderName) throws Exception {
    simulator.test("/patch/test")
             .withJSONFile(Path.of("src/test/resources/json/patch/test-patch.json"))
             .withHeader(overrideHeaderName, "PATCH")
             .post()
             .assertStatusCode(200)
             .assertHeaderContains("Cache-Control", "no-cache")
             .assertJSONFile(jsonDir.resolve("patch/test-response.json"), "config", "patched");
  }

  @Test
  public void patch_testing() throws Exception {
    // POST no big deal
    simulator.test("/patch/test")
             .withJSONFile(Path.of("src/test/resources/json/patch/test.json"), "config", "post")
             .post()
             .assertStatusCode(200)
             .assertJSONFile(jsonDir.resolve("patch/test-response.json"), "config", "post");

    // PUT no big deal
    simulator.test("/patch/test")
             .withJSONFile(Path.of("src/test/resources/json/patch/test.json"), "config", "put")
             .put()
             .assertStatusCode(200)
             .assertJSONFile(jsonDir.resolve("patch/test-response.json"), "config", "put");

    // PATCH damn that is cool
    simulator.test("/patch/test")
             .withJSONFile(Path.of("src/test/resources/json/patch/test-patch.json"))
             .patch()
             .assertStatusCode(200)
             .assertJSONFile(jsonDir.resolve("patch/test-response.json"), "config", "patched");
  }
}
