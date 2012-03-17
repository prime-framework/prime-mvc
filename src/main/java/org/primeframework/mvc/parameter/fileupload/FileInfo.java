/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.parameter.fileupload;

import java.io.File;

/**
 * This class provides file info for multipart requests.
 *
 * @author Brian Pontarelli
 */
public class FileInfo {
  public final File file;
  public final String name;
  public final String contentType;

  public FileInfo(File file, String name, String contentType) {
    this.file = file;
    this.name = name;
    this.contentType = contentType;
  }

  public File getFile() {
    return file;
  }

  public String getName() {
    return name;
  }

  public String getContentType() {
    return contentType;
  }

  public boolean deleteTempFile() {
    return file.delete();
  }
}
