package sonia.scm.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionIdTest {

  @Mock
  private HttpServletRequest request;

  @Test
  void shouldReturnSessionIdFromHeader() {
    when(request.getHeader(SessionId.PARAMETER)).thenReturn("abc42");

    assertThat(SessionId.from(request)).contains(SessionId.valueOf("abc42"));
  }

  @Test
  void shouldReturnSessionIdFromQueryParameter() {
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameter(SessionId.PARAMETER)).thenReturn("abc42");

    assertThat(SessionId.from(request)).contains(SessionId.valueOf("abc42"));
  }

  @Test
  void shouldReturnSessionIdFromQueryParameterOnlyForGetRequest() {
    when(request.getMethod()).thenReturn("POST");
    lenient().when(request.getParameter(SessionId.PARAMETER)).thenReturn("abc42");

    assertThat(SessionId.from(request)).isEmpty();
  }
}
