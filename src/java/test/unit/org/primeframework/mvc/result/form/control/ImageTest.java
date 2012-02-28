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
package org.primeframework.mvc.result.form.control;

import org.example.action.user.Edit;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.result.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This tests the image control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class ImageTest extends ControlBaseTest {
    @Inject public Image image;

    @Test
    public void testActionLess() {
        ais.setCurrent(new DefaultActionInvocation(null, "/image", null, null));
        run(image,
            mapNV("name", "image", "value", "test-value", "class", "css-class", "bundle", "/image-bundle", "src", "foo.gif"),
            null, "<input type=\"hidden\" name=\"image@param\" value=\"param-value\"/>\n" +
            "<input type=\"hidden\" name=\"__jc_a_image\" value=\"\"/>\n" +
            "<div class=\"css-class-image-button css-class-button css-class-control image-button button control\">\n" +
            "<div class=\"label-container\"> </div>\n" +
            "<div class=\"control-container\"><input type=\"image\" class=\"css-class\" id=\"image\" name=\"image\" src=\"foo.gif\" value=\"Image-Bundle\"/></div>\n" +
            "</div>\n");
    }

    @Test
    public void testAction() {
        ais.setCurrent(new DefaultActionInvocation(new Edit(), "/image", null, null));
        run(image,
            mapNV("name", "image", "value", "test-value", "src", "foo.gif"),
            null, "<input type=\"hidden\" name=\"image@param\" value=\"param-value\"/>\n" +
            "<input type=\"hidden\" name=\"__jc_a_image\" value=\"\"/>\n" +
            "<div class=\"image-button button control\">\n" +
            "<div class=\"label-container\"> </div>\n" +
            "<div class=\"control-container\"><input type=\"image\" id=\"image\" name=\"image\" src=\"foo.gif\" value=\"Image\"/></div>\n" +
            "</div>\n");
    }

    @Test
    public void html() {
        ais.setCurrent(new DefaultActionInvocation(new Edit(), "/image", null, null));
        run(image,
            mapNV("name", "html", "value", "test-value", "src", "foo.gif"),
            null, "<input type=\"hidden\" name=\"html@param\" value=\"param-value\"/>\n" +
            "<input type=\"hidden\" name=\"__jc_a_html\" value=\"\"/>\n" +
            "<div class=\"image-button button control\">\n" +
            "<div class=\"label-container\"> </div>\n" +
            "<div class=\"control-container\"><input type=\"image\" id=\"html\" name=\"html\" src=\"foo.gif\" value=\"&lt;Image&gt;\"/></div>\n" +
            "</div>\n");
    }

    @Test
    public void testActionAttribute() {
        ais.setCurrent(new DefaultActionInvocation(new Edit(), "/image", null, null));
        run(image,
            mapNV("name", "image", "action", "/foo", "value", "test-value", "src", "foo.gif"),
            null, "<input type=\"hidden\" name=\"image@param\" value=\"param-value\"/>\n" +
            "<input type=\"hidden\" name=\"__jc_a_image\" value=\"/foo\"/>\n" +
            "<div class=\"image-button button control\">\n" +
            "<div class=\"label-container\"> </div>\n" +
            "<div class=\"control-container\"><input type=\"image\" id=\"image\" name=\"image\" src=\"foo.gif\" value=\"Image\"/></div>\n" +
            "</div>\n");
    }

    @Test
    public void testIsmap() {
        ais.setCurrent(new DefaultActionInvocation(new Edit(), "/image", null, null));
        run(image,
            mapNV("name", "image", "value", "test-value", "ismap", true, "src", "foo.gif"),
            null, "<input type=\"hidden\" name=\"image@param\" value=\"param-value\"/>\n" +
            "<input type=\"hidden\" name=\"__jc_a_image\" value=\"\"/>\n" +
            "<div class=\"image-button button control\">\n" +
            "<div class=\"label-container\"> </div>\n" +
            "<div class=\"control-container\"><input type=\"image\" id=\"image\" ismap=\"ismap\" name=\"image\" src=\"foo.gif\" value=\"Image\"/></div>\n" +
            "</div>\n");
    }

    @Test
    public void testActionContext() {
        request.setContextPath("/context");
        ais.setCurrent(new DefaultActionInvocation(new Edit(), "/image", null, null));
        run(image,
            mapNV("name", "image", "value", "test-value", "action", "/foo", "src", "foo.gif"),
            null, "<input type=\"hidden\" name=\"image@param\" value=\"param-value\"/>\n" +
            "<input type=\"hidden\" name=\"__jc_a_image\" value=\"/context/foo\"/>\n" +
            "<div class=\"image-button button control\">\n" +
            "<div class=\"label-container\"> </div>\n" +
            "<div class=\"control-container\"><input type=\"image\" id=\"image\" name=\"image\" src=\"foo.gif\" value=\"Image\"/></div>\n" +
            "</div>\n");
    }

    @Test
    public void testActionContextRelative() {
        request.setContextPath("/context");
        ais.setCurrent(new DefaultActionInvocation(new Edit(), "/image", null, null));
        run(image,
            mapNV("name", "image", "value", "test-value", "action", "foo", "src", "foo.gif"),
            null, "<input type=\"hidden\" name=\"image@param\" value=\"param-value\"/>\n" +
            "<input type=\"hidden\" name=\"__jc_a_image\" value=\"foo\"/>\n" +
            "<div class=\"image-button button control\">\n" +
            "<div class=\"label-container\"> </div>\n" +
            "<div class=\"control-container\"><input type=\"image\" id=\"image\" name=\"image\" src=\"foo.gif\" value=\"Image\"/></div>\n" +
            "</div>\n");
    }
}