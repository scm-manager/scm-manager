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

  private static final String CREDENTIALS_WITH_XSRF = "eyJhbGciOiJIUzI1NiJ9.eyJ4c3JmIjoiZjlhMWRiNzQtM2UwNS00YTMwLTlkODMtNjZmNWQ1MDc3Y2FjIiwic3ViIjoic2NtYWRtaW4iLCJqdGkiOiI2d1JxTWpyelYxSCIsImlhdCI6MTU4MTU4MTI3OSwiZXhwIjoxNTgxNTg0ODc5LCJzY20tbWFuYWdlci5yZWZyZXNoRXhwaXJhdGlvbiI6MTU4MTYyNDQ3OTczMCwic2NtLW1hbmFnZXIucGFyZW50VG9rZW5JZCI6IjZ3UnFNanJ6VjFIIn0.O5MADk9scaHgYNPDFh7Nd9R2rMZyDuMs7LuC4OSA3jA";
  private static final String CREDENTIALS_WITHOUT_XSRF = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzY21hZG1pbiIsImp0aSI6IjdvUnFNbERrRTFOIiwiaWF0IjoxNTgxNTgxNjAxLCJleHAiOjE1ODE1ODUyMDEsInNjbS1tYW5hZ2VyLnJlZnJlc2hFeHBpcmF0aW9uIjoxNTgxNjI0ODAxNjc5LCJzY20tbWFuYWdlci5wYXJlbnRUb2tlbklkIjoiN29ScU1sRGtFMU4ifQ.KaTPjT09xtIEZDBOM28pSgyYSEtVZ37gcyTp1_3sTGA";

  @Mock
  HgRepositoryHandler handler;
  @Mock
  HgHookManager hookManager;

  @Test
  void shouldExtractXsrfTokenWhenSet() {
    when(hookManager.getCredentials()).thenReturn(CREDENTIALS_WITH_XSRF);

    Map<String, String> environment = new HashMap<>();
    HgEnvironment.prepareEnvironment(environment, handler, hookManager);

    assertThat(environment).contains(entry("SCM_XSRF", "f9a1db74-3e05-4a30-9d83-66f5d5077cac"));
  }

  @Test
  void shouldIgnoreXsrfWhenNotSet() {
    when(hookManager.getCredentials()).thenReturn(CREDENTIALS_WITHOUT_XSRF);

    Map<String, String> environment = new HashMap<>();
    HgEnvironment.prepareEnvironment(environment, handler, hookManager);

    assertThat(environment).doesNotContainKeys("SCM_XSRF");
  }
}
