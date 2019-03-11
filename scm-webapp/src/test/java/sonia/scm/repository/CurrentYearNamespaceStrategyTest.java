package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentYearNamespaceStrategyTest {

  @Mock
  private Clock clock;
  private NamespaceStrategy namespaceStrategy;

  @BeforeEach
  void setupObjectUnderTest() {
    namespaceStrategy = new CurrentYearNamespaceStrategy(clock);
  }

  @Test
  void shouldReturn1985() {
    LocalDateTime dateTime = LocalDateTime.of(1985, 4, 9, 21, 42);
    when(clock.instant()).thenReturn(dateTime.toInstant(ZoneOffset.UTC));
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());

    String namespace = namespaceStrategy.createNamespace(RepositoryTestData.createHeartOfGold());
    assertThat(namespace).isEqualTo("1985");
  }

}
