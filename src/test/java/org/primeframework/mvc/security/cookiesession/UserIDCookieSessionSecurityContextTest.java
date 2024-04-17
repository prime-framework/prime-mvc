/*
 * Copyright (c) 2024, Inversoft Inc., All Rights Reserved
 */
package org.primeframework.mvc.security.cookiesession;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

import com.google.inject.Inject;
import io.fusionauth.http.server.HTTPListenerConfiguration;
import io.fusionauth.http.server.HTTPServerConfiguration;
import org.primeframework.mvc.TestPrimeMain;
import org.primeframework.mvc.guice.MVCModule;
import org.primeframework.mvc.http.HTTPObjectsHolder;
import org.primeframework.mvc.message.TestMessageObserver;
import org.primeframework.mvc.security.cookiesession.UserIDCookieSessionSecurityContext.CookieExtendResult;
import org.primeframework.mvc.test.RequestResult;
import org.primeframework.mvc.test.RequestSimulator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public class UserIDCookieSessionSecurityContextTest {
  private static final String UserKey = UserIDCookieSessionSecurityContext.UserKey;

  private static ZonedDateTime mockClockNow;

  private static RequestSimulator simulator;

  private static Clock mockClock;

  private static void resetMockClock() {
    mockClock = Clock.fixed(Instant.ofEpochSecond(42), ZoneId.of("UTC"));
    mockClockNow = mockClock.instant().atZone(ZoneId.of("UTC"));
  }

  @Inject private SessionCookieKeyChanger cookieKeyChanger;

  // makes it easier to see why we "start" an HTTP server twice in test runs
  @AfterClass
  public void shutdown() {
    simulator.shutdown();
  }

  @AfterMethod
  public void afterMethod() {
    HTTPObjectsHolder.clearRequest();
    HTTPObjectsHolder.clearResponse();
  }

  @BeforeClass
  public void startItUp() {
    resetMockClock();
    simulator = buildSimulator(new SessionTestModule(() -> mockClock, true));
    simulator.getInjector().injectMembers(this);
  }

  private static RequestSimulator buildSimulator(SessionTestModule module) {
    // PrimeBaseTest uses 9080
    var httpConfig = new HTTPServerConfiguration().withListener(new HTTPListenerConfiguration(9081));
    var main = new TestPrimeMain(new HTTPServerConfiguration[]{httpConfig}, module, new MVCModule());
    return new RequestSimulator(main, new TestMessageObserver());
  }

  @BeforeMethod
  void cleanup() {
    simulator.reset();
    resetMockClock();
  }

  private RequestResult getSessionInfo() {
    var requestBuilder = simulator.test("/security/cookiesession/get-session-info");
    return requestBuilder.get();
  }

  private RequestResult doLogin(int expectedStatusCode) {
    return simulator.test("/security/cookiesession/do-login").get().assertStatusCode(expectedStatusCode);
  }

  @Test
  public void getCurrentUser_no_session() {
    // act + assert
    getSessionInfo().assertBodyContains("the current user is (no user)");
  }

  @Test
  public void getCurrentUser_has_session() {
    // arrange
    doLogin(200);

    // act + assert
    getSessionInfo().assertBodyContains("the current user is bob");
  }

  @Test
  public void getCurrentUser_has_session_caches_in_request() {
    // arrange
    doLogin(200);

    // act + assert
    getSessionInfo().assertBodyContains("the user in the request is bob");

  }

  @Test
  public void getCurrentUser_extend() {
    // arrange
    doLogin(200);
    var moreThanHalf = mockClockNow.plusMinutes(4);
    mockClock = Clock.fixed(moreThanHalf.toInstant(), ZoneId.of("UTC"));

    // act
    var result = getSessionInfo();

    // assert
    // normally the cookie will not be set again with a different max age
    result.assertContainsCookie(UserKey);
  }

  @Test
  public void getCurrentUser_no_extend() {
    // arrange
    doLogin(200);
    var pastAge = mockClockNow.plusMinutes(31);
    mockClock = Clock.fixed(pastAge.toInstant(), ZoneId.of("UTC"));

    // act
    var result = getSessionInfo();

    // assert
    // normally the cookie will not be set again with a different max age
    result.assertBodyContains("the current user is (no user)");
    assertEquals(result.getCookie(UserKey).value, "null");
  }

  @Test
  public void getSessionId_no_session() {
    // act + assert
    getSessionInfo().assertBodyContains("the session ID is (no session)");
  }

  @Test
  public void getSessionId_has_session() {
    // arrange
    doLogin(200);

    // act + assert
    getSessionInfo().assertBodyDoesNotContain("the session ID is (no session)");
  }

  @Test
  public void isLoggedIn_no_session() {
    // act + assert
    getSessionInfo().assertBodyContains("logged in no");
  }

  @Test
  public void isLoggedIn_has_session() {
    // arrange
    doLogin(200);

    // act + assert
    getSessionInfo().assertBodyContains("logged in yes");
  }

  @Test
  public void login() {
    // arrange

    // act
    var result = doLogin(200);

    // assert
    result.assertStatusCode(200).assertContainsNoGeneralErrors().assertContainsCookie(UserKey);
  }

  @Test
  public void login_without_jackson_modules() {
    // arrange
    simulator.shutdown();
    try {
      simulator = buildSimulator(new SessionTestModule(() -> mockClock, false));
      // act
      var result = doLogin(500);

      // assert
      result.assertBodyContains("java.lang.IllegalStateException: You are missing a Jackson module that serializes ZonedDateTime");
    } finally {
      // repair our @BeforeClass state
      simulator.shutdown();
      simulator = buildSimulator(new SessionTestModule(() -> mockClock, true));
      simulator.getInjector().injectMembers(this);
    }
  }

  @Test
  public void logout() {
    // arrange
    doLogin(200);

    // act
    simulator.test("/security/cookiesession/do-logout").get();

    // assert
    getSessionInfo().assertBodyContains("the session ID is (no session)").assertBodyContains("logged in no").assertBodyContains("the current user is (no user)");
  }

  private static String getSessionID(RequestResult response) {
    var pattern = Pattern.compile(".*the session ID is (\\S+).*", Pattern.MULTILINE | Pattern.DOTALL);
    var matcher = pattern.matcher(response.getBodyAsString());
    assertTrue(matcher.matches());
    return matcher.group(1);
  }

  @Test
  public void updateUser() {
    // arrange
    doLogin(200);
    var baselineGet = getSessionInfo();
    var previousSessionId = getSessionID(baselineGet);

    // act
    var updateResponse = simulator.test("/security/cookiesession/get-session-info").withURLParameter("update", "yes").withURLParameter("updateNewUserEmail", "alice").get();

    // assert
    updateResponse.assertBodyContains("the current user is alice")
                  // want to ensure our session ID stays the same
                  .assertBodyContains("the session ID is " + previousSessionId);
    // ensure with a fresh GET, our session is consistent, but given we hydrate the user from the DB
    // we would expect the current user to go back to Bob
    getSessionInfo().assertBodyContains("the session ID is " + previousSessionId).assertBodyContains("the current user is bob");
  }

  @Test
  public void cookie_encryption_key_changed() {
    // arrange
    doLogin(200);
    // if this key changes, the user's browser will still supply a cookie, but it will fail to be decrypted
    // and if not handled properly, will stack trace and the user cannot get back in

    // act
    var existingCookie = simulator.userAgent.getCookie(UserKey);
    cookieKeyChanger.changeIt(existingCookie);

    // assert
    getSessionInfo().assertBodyContains("the session ID is (no session)").assertBodyContains("logged in no").assertBodyContains("the current user is (no user)");
  }

  @DataProvider(name = "cookieActionData")
  public Object[][] extendCookieData() {
    return new Object[][]{{"less_than_halfway_through_timeout", mockClockNow, Duration.ofDays(1), Duration.ofMinutes(30), CookieExtendResult.Keep}, {"halfway_through_timeout", mockClockNow.minusMinutes(15), Duration.ofDays(1), Duration.ofMinutes(30), CookieExtendResult.Keep}, {"more_than_halfway_through_timeout", mockClockNow.minusMinutes(16), Duration.ofDays(1), Duration.ofMinutes(30), CookieExtendResult.Extend}, {"almost_max_age", mockClockNow.minusMinutes(10), Duration.ofMinutes(30), Duration.ofMinutes(30), CookieExtendResult.Keep}, {"equals_max_age", mockClockNow.minusMinutes(30), Duration.ofMinutes(30), Duration.ofMinutes(30), CookieExtendResult.Keep}, {"past_max_age", mockClockNow.minusMinutes(60), Duration.ofMinutes(30), Duration.ofMinutes(30), CookieExtendResult.Invalid}};
  }

  @Test(dataProvider = "cookieActionData")
  public void shouldExtendCookie(String label, ZonedDateTime signInTime, Duration maxSessionAge, Duration sessionTimeout,
                                 CookieExtendResult expectedResult) {
    // arrange
    // we don't need most of these dependencies to test this
    var securityContext = new MockUserIDCookieSession(null, null, null, null, mockClock, sessionTimeout, maxSessionAge);

    // act
    var actualResult = securityContext.shouldExtendCookie(signInTime);

    // assert
    assertEquals(actualResult, expectedResult);
  }
}
