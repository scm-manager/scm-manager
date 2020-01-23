package sonia.scm.api.v2.resources;

import com.github.sdorra.spotter.Language;

import java.util.Optional;

final class ProgrammingLanguages {

  static final String HEADER = "X-Programming-Language";

  private static final String DEFAULT = "text";

  private ProgrammingLanguages() {
  }

  static String getValue(Language language) {
    Optional<String> aceMode = language.getAceMode();
    if (!aceMode.isPresent()) {
      Optional<String> codemirrorMode = language.getCodemirrorMode();
      return codemirrorMode.orElse(DEFAULT);
    }
    return aceMode.get();
  }
}
