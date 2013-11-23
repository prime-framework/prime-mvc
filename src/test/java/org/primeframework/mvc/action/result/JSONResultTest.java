package org.primeframework.mvc.action.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.example.action.Post;
import org.example.domain.AddressField;
import org.example.domain.UserField;
import org.example.domain.UserType;
import org.primeframework.mock.servlet.MockServletOutputStream;
import org.primeframework.mvc.PrimeBaseTest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.config.ActionConfiguration;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.XMLStream;
import org.primeframework.mvc.content.json.JacksonActionConfiguration;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * This class tests the JSON result.
 *
 * @author Brian Pontarelli
 */
public class JSONResultTest extends PrimeBaseTest {
  @Inject public ObjectMapper objectMapper;

  @Test
  public void all() throws IOException, ServletException {
    UserField userField = new UserField();
    userField.addresses.put("work", new AddressField());
    userField.addresses.get("work").city = "Denver";
    userField.addresses.get("work").state = "Colorado";
    userField.addresses.get("work").zipcode = "80202";
    userField.addresses.put("home", new AddressField());
    userField.addresses.get("home").city = "Broomfield";
    userField.addresses.get("home").state = "Colorado";
    userField.addresses.get("home").zipcode = "80023";
    userField.active = true;
    userField.age = 37;
    userField.favoriteMonth = 5;
    userField.favoriteYear = 1976;
    userField.ids.put(0, 1);
    userField.ids.put(1, 2);
    userField.lifeStory = "Hello world";
    userField.securityQuestions = new String[]{"one", "two", "three", "four"};
    userField.siblings.add(new UserField("Brett"));
    userField.siblings.add(new UserField("Beth"));
    userField.type = UserType.COOL;

    Post action = new Post();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.getValue("user", action)).andReturn(userField);
    replay(ee);

    MockServletOutputStream sos = new MockServletOutputStream();
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(200);
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setContentLength(389);
    expect(response.getOutputStream()).andReturn(sos);
    replay(response);

    Map<Class<?>, Object> additionalConfiguration = new HashMap<Class<?>, Object>();
    additionalConfiguration.put(JacksonActionConfiguration.class, new JacksonActionConfiguration(null, null, "user", UserField.class));
    ActionConfiguration config = new ActionConfiguration(Post.class, null, null, null, null, null, null, null, null, null, null, null, null, additionalConfiguration, null);
    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, null, "/foo", "", config));
    replay(store);

    JSON annotation = new JSONResultTest.JSONImpl("success", 200);
    JSONResult result = new JSONResult(ee, response, store, objectMapper);
    result.execute(annotation);

    String expected = "{" +
        "  \"active\":true," +
        "  \"addresses\":{" +
        "    \"home\":{" +
        "      \"city\":\"Broomfield\"," +
        "      \"state\":\"Colorado\"," +
        "      \"zipcode\":\"80023\"" +
        "    }," +
        "    \"work\":{" +
        "      \"city\":\"Denver\"," +
        "      \"state\":\"Colorado\"," +
        "      \"zipcode\":\"80202\"" +
        "    }" +
        "  }," +
        "  \"age\":37," +
        "  \"favoriteMonth\":5," +
        "  \"favoriteYear\":1976," +
        "  \"ids\":{" +
        "    \"0\":1," +
        "    \"1\":2" +
        "  }," +
        "  \"lifeStory\":\"Hello world\"," +
        "  \"securityQuestions\":[\"one\",\"two\",\"three\",\"four\"]," +
        "  \"siblings\":[{" +
        "    \"active\":false," +
        "    \"name\":\"Brett\"" +
        "  },{" +
        "    \"active\":false," +
        "    \"name\":\"Beth\"" +
        "  }]," +
        "  \"type\":\"COOL\"" +
        "}";
    assertEquals(sos.toString(), expected.replace("  ", "")); // Un-indent

    verify(ee, response);
  }

  public class JSONImpl implements JSON {
    private final String code;
    private final int status;

    public JSONImpl(String code, int status) {
      this.code = code;
      this.status = status;
    }

    @Override
    public String code() {
      return code;
    }

    @Override
    public int status() {
      return status;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return XMLStream.class;
    }
  }
}
