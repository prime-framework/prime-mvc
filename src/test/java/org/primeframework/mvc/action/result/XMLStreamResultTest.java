package org.primeframework.mvc.action.result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.primeframework.mock.servlet.MockServletOutputStream;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.action.result.annotation.XMLStream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

/**
 * This class tests the XML Stream result.
 *
 * @author jhumphrey
 */
public class XMLStreamResultTest {
  @Test
  public void explicit() throws IOException, ServletException {
    String property = "xml";
    String propertyValue = "<xml/>";
    byte[] propertyBytes = propertyValue.getBytes();
    int propertyBytesLen = propertyBytes.length;
    String contentType = "application/xhtml+xml";

    Object action = new Object();
    ExpressionEvaluator ee = createStrictMock(ExpressionEvaluator.class);
    expect(ee.getValue(property, action)).andReturn(propertyValue);
    replay(ee);

    MockServletOutputStream sos = new MockServletOutputStream();
    HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    response.setStatus(200);
    response.setCharacterEncoding("UTF-8");
    response.setContentType(contentType);
    response.setContentLength(propertyBytesLen);
    expect(response.getOutputStream()).andReturn(sos);
    replay(response);

    ActionInvocationStore store = createStrictMock(ActionInvocationStore.class);
    expect(store.getCurrent()).andReturn(new ActionInvocation(action, null, "/foo", "", null));
    replay(store);

    XMLStream xmlStream = new XMLStreamResultTest.XMLStreamImpl("success", "xml", 200);
    XMLStreamResult streamResult = new XMLStreamResult(ee, response, store);
    streamResult.execute(xmlStream);

    assertEquals(sos.toString(), "<xml/>");

    verify(ee, response);
  }

  public class XMLStreamImpl implements XMLStream {
    private final String code;
    private final String property;
    private final int status;

    public XMLStreamImpl(String code, String property, int status) {
      this.code = code;
      this.property = property;
      this.status = status;
    }

    public String code() {
      return code;
    }

    public String property() {
      return property;
    }

    public int status() {
      return status;
    }

    public Class<? extends Annotation> annotationType() {
      return XMLStream.class;
    }
  }
}
