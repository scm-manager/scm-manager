package sonia.scm.api.v2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Date;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CacheControlResponseFilterTest {

  @Mock
  private ContainerRequestContext requestContext;

  @Mock
  private ContainerResponseContext responseContext;

  @Mock
  private MultivaluedMap<String, Object> headers;

  private CacheControlResponseFilter filter = new CacheControlResponseFilter();

  @Before
  public void setUpMocks() {
    when(responseContext.getHeaders()).thenReturn(headers);
  }

  @Test
  public void filterShouldAddCacheControlHeader() {
    filter.filter(requestContext, responseContext);

    verify(headers).add("Cache-Control", "no-cache");
  }

  @Test
  public void filterShouldNotSetHeaderIfLastModifiedIsNotNull() {
    when(responseContext.getLastModified()).thenReturn(new Date());

    filter.filter(requestContext, responseContext);

    verify(headers, never()).add("Cache-Control", "no-cache");
  }

  @Test
  public void filterShouldNotSetHeaderIfEtagIsNotNull() {
    when(responseContext.getEntityTag()).thenReturn(new EntityTag("42"));

    filter.filter(requestContext, responseContext);

    verify(headers, never()).add("Cache-Control", "no-cache");
  }

}
