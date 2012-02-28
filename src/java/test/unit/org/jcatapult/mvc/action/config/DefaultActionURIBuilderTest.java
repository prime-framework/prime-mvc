/*
 * Copyright (c) 2001-2007, JCatapult.org, All Rights Reserved
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
package org.jcatapult.mvc.action.config;

import static org.junit.Assert.*;
import org.junit.Test;
import org.jcatapult.mvc.util.DefaultURIBuilder;

/**
 * <p>
 * This class tests the default action URI builder.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class DefaultActionURIBuilderTest {
    /**
     * Tests the URI builder.
     */
    @Test
    public void testBuild() {
        DefaultURIBuilder builder = new DefaultURIBuilder();
        assertEquals("/config/default-action-uri-builder-test", builder.build(this.getClass()));
    }
}