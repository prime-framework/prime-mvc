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
package org.primeframework.mvc.validation.jsr303.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.primeframework.mvc.validation.jsr303.constraint.URLConnectivity;

/**
 * Validates URL connectivity
 *
 * @author James Humphrey
 */
public class URLConnectivityValidator implements ConstraintValidator<URLConnectivity, String> {

  @Override
  public void initialize(URLConnectivity constraintAnnotation) {
  }

  @Override
  public boolean isValid(String url, ConstraintValidatorContext context) {
    if (StringUtils.isBlank(url)) {
      return true;
    }

    try {
      new URL(url);
    } catch (MalformedURLException e) {
      return true;
    }

    return isAvailable(url);
  }

  public boolean isAvailable(String urlString) {
    try {
      URL url = new URL(urlString);
      HttpURLConnection huc = (HttpURLConnection) url.openConnection();
      huc.setDoOutput(true);
      huc.setDoInput(true);
      huc.setRequestMethod("GET");

      int code = huc.getResponseCode();
      return code == 200;
    } catch (IOException e) {
      return false;
    }
  }
}
