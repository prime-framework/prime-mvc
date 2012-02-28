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
package org.primeframework.mvc.result.form.control;

import org.example.action.user.Edit;
import org.example.domain.User;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.result.control.ControlBaseTest;
import org.testng.annotations.Test;

import com.google.inject.Inject;
import static net.java.util.CollectionTools.*;

/**
 * <p>
 * This tests the months select control.
 * </p>
 *
 * @author  Brian Pontarelli
 */
public class YearsSelectTest extends ControlBaseTest {
    @Inject public YearsSelect yearsSelect;

    @Test
    public void testActionLess() {
        ais.setCurrent(new DefaultActionInvocation(null, "/years-select", null, null));
        run(yearsSelect,
            mapNV("name", "test", "class", "css-class", "bundle", "/years-select-bundle"),
            null, "<input type=\"hidden\" name=\"test@param\" value=\"param-value\"/>\n" +
            "<div class=\"css-class-select css-class-input css-class-control select input control\">\n" +
            "<div class=\"label-container\"><label for=\"test\" class=\"label\">Test</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select class=\"css-class\" id=\"test\" name=\"test\">\n" +
            "<option value=\"2011\">2011</option>\n" +
            "<option value=\"2012\">2012</option>\n" +
            "<option value=\"2013\">2013</option>\n" +
            "<option value=\"2014\">2014</option>\n" +
            "<option value=\"2015\">2015</option>\n" +
            "<option value=\"2016\">2016</option>\n" +
            "<option value=\"2017\">2017</option>\n" +
            "<option value=\"2018\">2018</option>\n" +
            "<option value=\"2019\">2019</option>\n" +
            "<option value=\"2020\">2020</option>\n" +
            "</select>\n" +
            "</div>\n" +
            "</div>\n");
    }

    @Test
    public void testAction() {
        Edit edit = new Edit();
        edit.user = new User();
        edit.user.setYear(2003);

        ais.setCurrent(new DefaultActionInvocation(edit, "/years-select", null, null));
        run(yearsSelect,
            mapNV("name", "user.year", "startYear", 2001, "endYear", 2006),
            null, "<input type=\"hidden\" name=\"user.year@param\" value=\"param-value\"/>\n" +
            "<div class=\"select input control\">\n" +
            "<div class=\"label-container\"><label for=\"user_year\" class=\"label\">Year</label></div>\n" +
            "<div class=\"control-container\">\n" +
            "<select id=\"user_year\" name=\"user.year\">\n" +
            "<option value=\"2001\">2001</option>\n" +
            "<option value=\"2002\">2002</option>\n" +
            "<option value=\"2003\" selected=\"selected\">2003</option>\n" +
            "<option value=\"2004\">2004</option>\n" +
            "<option value=\"2005\">2005</option>\n" +
            "<option value=\"2006\">2006</option>\n" +
            "</select>\n" +
            "</div>\n" +
            "</div>\n");
    }
}
