package sonia.scm.repository;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgEnvironmentTest {

  private static final String CREDENTIALS_WITH_XSRF = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZW50Iiwic2NtLW1hbmFnZXIucGFyZW50VG9rZW5JZCI6IkFCQyIsInhzcmYiOiJYU1JGIFRva2VuIiwiaWF0IjoxNTgxNTg3MzUzLCJqdGkiOiJFV1JxTjlNMTQ5In0.jgsIoE_2TnTEwbuaqQp8XyKpId5qlYURmYamf9m_08w";
  private static final String CREDENTIALS_WITHOUT_XSRF = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZW50Iiwic2NtLW1hbmFnZXIucGFyZW50VG9rZW5JZCI6IkFCQyIsImlhdCI6MTU4MTU4NzM1MywianRpIjoiRVdScU45TTE0OSJ9.VdMz5-NpREiIvLEw9JVJNEUnoY0am0j1lZ0kisblayk";

  @Mock
  HgRepositoryHandler handler;
  @Mock
  HgHookManager hookManager;

  @Test
  void shouldExtractXsrfTokenWhenSet() {
    when(hookManager.getCredentials()).thenReturn(CREDENTIALS_WITH_XSRF);

    Map<String, String> environment = new HashMap<>();
    HgEnvironment.prepareEnvironment(environment, handler, hookManager);

    assertThat(environment).contains(entry("SCM_XSRF", "XSRF Token"));
  }

  @Test
  void shouldIgnoreXsrfWhenNotSetButStillContainDummy() {
    when(hookManager.getCredentials()).thenReturn(CREDENTIALS_WITHOUT_XSRF);

    Map<String, String> environment = new HashMap<>();
    HgEnvironment.prepareEnvironment(environment, handler, hookManager);

    assertThat(environment).containsKeys("SCM_XSRF");
  }
}
