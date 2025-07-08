/*
 * Copyright (c) 2012-2025, Inversoft Inc., All Rights Reserved
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
package org.example.action.user;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import io.fusionauth.http.FileInfo;
import io.fusionauth.http.server.HTTPRequest;
import org.example.domain.Role;
import org.example.domain.UserField;
import org.example.domain.UserType;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import static java.util.Arrays.asList;

/**
 * This class is a simple edit action for testing.
 *
 * @author Brian Pontarelli
 */
@Action
public class FullFormAction {
  public static List<Integer> agesFromLastPost;

  public static FileInfo imageFromLastPost;

  public static List<Integer> roleIdsFromLastPost;

  public static String stringFieldFromLastPost;

  private final HTTPRequest request;

  public List<Integer> ages = new ArrayList<>();

  @FileUpload(contentTypes = {"*"})
  public FileInfo image;

  public List<Integer> roleIds;

  public List<Role> roles = asList(new Role(1, "Admin"), new Role(2, "User"));

  public String stringField;

  public UserField user;

  public UserType[] userTypes = UserType.values();

  @Inject
  public FullFormAction(HTTPRequest request) {
    this.request = request;
    for (int i = 1; i < 100; i++) {
      ages.add(i);
    }
    reset();
  }

  public static void reset() {
    if (imageFromLastPost != null) {
      try {
        Files.deleteIfExists(imageFromLastPost.getFile());
      } catch (IOException ignore) {
      }
    }

    roleIdsFromLastPost = null;
    imageFromLastPost = null;
    agesFromLastPost = null;
    stringFieldFromLastPost = null;
  }

  public String get() {
    return "input";
  }

  public String post() {
    // Do not delete files automatically for this request
    request.getMultiPartStreamProcessor().getMultiPartConfiguration().withDeleteTemporaryFiles(false);
    roleIdsFromLastPost = roleIds;
    imageFromLastPost = image;
    if (imageFromLastPost != null) {
      imageFromLastPost.getFile().toFile().deleteOnExit();
    }
    agesFromLastPost = ages;
    stringFieldFromLastPost = stringField;
    return "success";
  }
}
