package sonia.scm.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class})
class InitialRepositoryLocationResolverTest {

  private InitialRepositoryLocationResolver resolver = new InitialRepositoryLocationResolver();

  @Test
  void shouldComputeInitialPath() {
    Path path = resolver.getPath("42");

    assertThat(path).isRelative();
    assertThat(path.toString()).isEqualTo("repositories" + File.separator + "42");
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfIdHasASlash() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.getPath("../../../passwd"));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfIdHasABackSlash() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.getPath("..\\..\\..\\users.ntlm"));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfIdIsDotDot() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.getPath(".."));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionIfIdIsDot() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> resolver.getPath("."));
  }
}
