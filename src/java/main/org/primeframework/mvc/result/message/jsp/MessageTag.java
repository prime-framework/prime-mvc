/*
 * Copyright (c) 2001-2007, Inversoft Inc., All Rights Reserved
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
package org.primeframework.mvc.result.message.jsp;

import org.primeframework.mvc.message.l10n.MessageProvider;
import org.primeframework.mvc.result.jsp.AbstractControlTag;
import org.primeframework.mvc.result.message.control.Message;

/**
 * <p> This class is a JSP taglib that can retrieve messages from the {@link MessageProvider} and output them. </p>
 *
 * @author Brian Pontarelli
 */
public class MessageTag extends AbstractControlTag<Message> {

  /**
   * @return The tags key attribute that is the key of the message to fetch.
   */
  public String getKey() {
    return (String) attributes.get("key");
  }

  /**
   * Populates the tags key attribute that is the key of the message to fetch.
   *
   * @param key The key.
   */
  public void setKey(String key) {
    attributes.put("key", key);
  }

  /**
   * @return Message.class
   */
  @Override
  protected Class<Message> controlClass() {
    return Message.class;
  }
}