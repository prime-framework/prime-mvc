package org.primeframework.mvc.action.result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.easymock.EasyMock;
import org.primeframework.mvc.action.DefaultActionInvocation;
import org.primeframework.mvc.action.result.annotation.XMLStream;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mock.servlet.MockServletOutputStream;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * <p> This class tests the XML Stream result. </p>
 *
 * @author jhumphrey
 */
public class XMLStreamResultTest {
  @Test
  public void testExplicit() throws IOException, ServletException {

    String property = "xml";
    String propertyValue = "<xml/>";
    byte[] propertyBytes = propertyValue.getBytes();
    int propertyBytesLen = propertyBytes.length;
    String contentType = "application/xhtml+xml";

    Object action = new Object();
    ExpressionEvaluator ee = EasyMock.createStrictMock(ExpressionEvaluator.class);
    EasyMock.expect(ee.getValue(property, action)).andReturn(propertyValue);
    EasyMock.replay(ee);

    MockServletOutputStream sos = new MockServletOutputStream();
    HttpServletResponse response = EasyMock.createStrictMock(HttpServletResponse.class);
    response.setStatus(200);
    response.setCharacterEncoding("UTF-8");
    response.setContentType(contentType);
    response.setContentLength(propertyBytesLen);
    EasyMock.expect(response.getOutputStream()).andReturn(sos);
    EasyMock.replay(response);

    XMLStream xmlStream = new XMLStreamResultTest.XMLStreamImpl("success", "xml", 200);
    XMLStreamResult streamResult = new XMLStreamResult(ee, response);
    streamResult.execute(xmlStream, new DefaultActionInvocation(action, "/foo", "", null));

    assertEquals("<xml/>", sos.toString());

    EasyMock.verify(ee, response);
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
