/*
 * Copyright (c) 2012, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.validation.jsr303.validator;

import java.lang.annotation.Annotation;

import org.primeframework.mvc.validation.jsr303.constraint.FieldsMatch;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author James Humphrey
 */
public class FieldsMatchValidatorTest extends BaseValidatorUnitTest {

  @Test
  public void isValid() {
    MockValidFormattedBean bean = new MockValidFormattedBean();
    bean.password = "foo";
    bean.confirmPassword = "foo";

    FieldsMatchValidator validator = new FieldsMatchValidator();
    validator.initialize(new MockFieldsMatch("password", "confirmPassword"));

    // assert valid with both strings not null and equal
    assertTrue(validator.isValid(bean, null));

    // assert valid with both strings null
    bean.password = null;
    bean.confirmPassword = null;
    assertTrue(validator.isValid(bean, null));

    // assert invalid when valid strings but not equal
    bean.password = "foo";
    bean.confirmPassword = "bar";
    assertFalse(validator.isValid(bean, makeContext("confirmPassword")));

    // assert with password not null and confirm password null
    bean.password = "foo";
    bean.confirmPassword = null;
    assertFalse(validator.isValid(bean, makeContext("confirmPassword")));

    // assert that if the password is null, then it's valid.  basically, this says that the validator only cares
    // about confirmPassword equality iff the password is not null
    bean.password = null;
    bean.confirmPassword = "bar";
    assertTrue(validator.isValid(bean, null));
  }

  @Test
  public void failures() {
    MockValidFormattedBean bean = new MockValidFormattedBean();
    bean.password = "foo";
    bean.confirmPassword = "foo";

    FieldsMatchValidator validator = new FieldsMatchValidator();

    // Too few fields
    try {
      validator.initialize(new MockFieldsMatch("password"));
      fail("Should have failed");
    } catch (Exception e) {
      // Expected
    }

    validator.initialize(new MockFieldsMatch("password", "bad"));

    // Bad field
    try {
      assertTrue(validator.isValid(bean, null));
      fail("Should have failed");
    } catch (Exception e) {
      // Expected
    }
  }

  /**
   * Mocks out a valid bean.  A valid bean is one that has fields names 'password' and 'confirmPassword'
   */
  public static class MockValidFormattedBean {
    public String password;
    public String confirmPassword;
  }

  public static class MockFieldsMatch implements FieldsMatch {
    public final String[] fields;

    public MockFieldsMatch(String... fields) {
      this.fields = fields;
    }


    @Override
    public String message() {
      return null;
    }

    @Override
    public Class[] groups() {
      return new Class[0];
    }

    @Override
    public Class[] payload() {
      return new Class[0];
    }

    @Override
    public String[] fields() {
      return fields;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return null;
    }
  }
}
