package sonia.scm.api.v2.resources;

import com.github.sdorra.spotter.Language;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProgrammingLanguagesTest {

  @Test
  void shouldReturnAceModeIfPresent() {
    assertThat(ProgrammingLanguages.getValue(Language.GO)).isEqualTo("golang");
    assertThat(ProgrammingLanguages.getValue(Language.JAVA)).isEqualTo("java");
  }

  @Test
  void shouldReturnCodemirrorIfAceModeIsMissing() {
    assertThat(ProgrammingLanguages.getValue(Language.HTML_ECR)).isEqualTo("htmlmixed");
  }

  @Test
  void shouldReturnTextIfNoModeIsPresent() {
    assertThat(ProgrammingLanguages.getValue(Language.HXML)).isEqualTo("text");
  }
}
