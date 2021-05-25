package io.honeybadger.reporter.spring;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import io.honeybadger.reporter.UnitTestExpectedException;
import io.honeybadger.reporter.config.SpringConfigContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;

/**
 * Tests {@link HoneybadgerSpringExceptionHandler}.
 */
public class HoneybadgerSpringExceptionHandlerTest {
  private final SpringConfigContext context = new SpringConfigContext(null);
  private final HttpServletRequest request = mock(HttpServletRequest.class);

  @Test
  public void handlerRethrowsExcludedExceptionsTest() {
    context.setApiKey("api-key");
    context.setExcludedClasses(ImmutableSet.of(
      "io.honeybadger.reporter.UnitTestExpectedException"));
    HoneybadgerSpringExceptionHandler handler = new HoneybadgerSpringExceptionHandler(context);

    assertThrows(UnitTestExpectedException.class,
      () -> handler.defaultErrorHandler(request, new UnitTestExpectedException()));
  }
}
