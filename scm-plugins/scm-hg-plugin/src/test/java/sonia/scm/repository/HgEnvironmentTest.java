package sonia.scm.repository;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.Xsrf;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgEnvironmentTest {

  @Mock
  HgRepositoryHandler handler;
  @Mock
  HgHookManager hookManager;

  @Test
  void shouldExtractXsrfTokenWhenSet() {
    AccessToken accessToken = mock(AccessToken.class);
    when(accessToken.compact()).thenReturn("");
    when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(of("XSRF Token"));
    when(hookManager.getAccessToken()).thenReturn(accessToken);

    Map<String, String> environment = new HashMap<>();
    HgEnvironment.prepareEnvironment(environment, handler, hookManager);

    assertThat(environment).contains(entry("SCM_XSRF", "XSRF Token"));
  }

  @Test
  void shouldIgnoreXsrfWhenNotSetButStillContainDummy() {
    AccessToken accessToken = mock(AccessToken.class);
    when(accessToken.compact()).thenReturn("");
    when(accessToken.getCustom(Xsrf.TOKEN_KEY)).thenReturn(empty());
    when(hookManager.getAccessToken()).thenReturn(accessToken);

    Map<String, String> environment = new HashMap<>();
    HgEnvironment.prepareEnvironment(environment, handler, hookManager);

    assertThat(environment).containsKeys("SCM_XSRF");
  }
}
