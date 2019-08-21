/*
 * Copyright (c) 2001-2019, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.action.result;

import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

import com.google.inject.Inject;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.config.MVCConfiguration;
import org.primeframework.mvc.freemarker.FreeMarkerMap;
import org.primeframework.mvc.freemarker.FreeMarkerService;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

/**
 * This result renders a FreeMarker template depending on the extension of the page.
 *
 * @author Brian Pontarelli
 */
public class ForwardResult extends AbstractForwardResult<Forward> {
  @Inject
  public ForwardResult(ActionInvocationStore actionInvocationStore, ExpressionEvaluator expressionEvaluator,
                       ResourceLocator resourceLocator, FreeMarkerService freeMarkerService,
                       HttpServletResponse response,
                       FreeMarkerMap freeMarkerMap, MVCConfiguration configuration) {
    super(actionInvocationStore, expressionEvaluator, resourceLocator, freeMarkerService, response, freeMarkerMap, configuration);
  }

  @Override
  protected String getCode(Forward forward) {
    return forward.code();
  }

  @Override
  protected String getContentType(Forward forward) {
    return forward.contentType();
  }

  @Override
  protected String getPage(Forward forward) {
    return forward.page();
  }

  @Override
  protected int getStatus(Forward forward) {
    return forward.status();
  }

  @Override
  protected String getStatusStr(Forward forward) {
    return forward.statusStr();
  }

  public static class ForwardImpl implements Forward {
    private final String code;

    private final String contentType;

    private final int status;

    private final String statusStr;

    private final String uri;

    public ForwardImpl(String uri, String code) {
      this.uri = uri;
      this.code = code;
      this.contentType = "text/html; charset=UTF-8";
      this.status = 200;
      this.statusStr = "";
    }

    public ForwardImpl(String uri, String code, String contentType, int status) {
      this.uri = uri;
      this.code = code;
      this.contentType = contentType;
      this.status = status;
      this.statusStr = "";
    }

    public Class<? extends Annotation> annotationType() {
      return Forward.class;
    }

    @Override
    public String code() {
      return code;
    }

    @Override
    public String contentType() {
      return contentType;
    }

    @Override
    public String page() {
      return uri;
    }

    @Override
    public int status() {
      return status;
    }

    @Override
    public String statusStr() {
      return statusStr;
    }
  }
}
