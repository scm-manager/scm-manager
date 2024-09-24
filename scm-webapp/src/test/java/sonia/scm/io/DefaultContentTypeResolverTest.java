/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.io;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultContentTypeResolverTest {

  private final DefaultContentTypeResolver contentTypeResolver = new DefaultContentTypeResolver(Collections.emptySet());

  @Test
  void shouldReturnPrimaryPart() {
    ContentType contentType = contentTypeResolver.resolve("hog.pdf");
    assertThat(contentType.getPrimary()).isEqualTo("application");
  }

  @Test
  void shouldReturnSecondaryPart() {
    ContentType contentType = contentTypeResolver.resolve("hog.pdf");
    assertThat(contentType.getSecondary()).isEqualTo("pdf");
  }

  @Test
  void shouldReturnRaw() {
    ContentType contentType = contentTypeResolver.resolve("hog.pdf");
    assertThat(contentType.getRaw()).isEqualTo("application/pdf");
  }

  @Test
  void shouldReturnContentTypeFromExtension() {
    DefaultContentTypeResolver contentTypeResolver = new DefaultContentTypeResolver(ImmutableSet.of((path, contentPrefix) -> Optional.of("scm/test")));

    ContentType contentType = contentTypeResolver.resolve("hog.pdf");
    assertThat(contentType.getRaw()).isEqualTo("scm/test");
  }

  @Nested
  class IsTextTests {

    @ParameterizedTest(name = "shouldReturnIsTextFor: {argumentsWithNames}")
    @ValueSource(strings = {"App.java", "Dockerfile", "Playbook.yml", "README.md", "LICENSE.txt"})
    void shouldReturnIsTextFor(String path) {
      ContentType contentType = contentTypeResolver.resolve(path);
      assertThat(contentType.isText()).isTrue();
    }

    @ParameterizedTest(name = "shouldReturnIsNotTextFor: {argumentsWithNames}")
    @ValueSource(strings = {"scan.exe", "hog.pdf", "library.so", "awesome.dll", "something.dylib"})
    void shouldReturnIsNotTextFor(String path) {
      ContentType contentType = contentTypeResolver.resolve(path);
      assertThat(contentType.isText()).isFalse();
    }
  }

  @Nested
  class LanguageTests {

    @Test
    void shouldResolveMarkdown() {
      String content = String.join("\n",
        "% Markdown content",
        "% Which does not start with markdown"
      );
      ContentType contentType = contentTypeResolver.resolve("somedoc.md", content.getBytes(StandardCharsets.UTF_8));
      assertThat(contentType.getLanguage()).contains("Markdown");
    }

    @Test
    void shouldResolveMarkdownWithoutContent() {
      ContentType contentType = contentTypeResolver.resolve("somedoc.md");
      assertThat(contentType.getLanguage()).contains("Markdown");
    }

    @Test
    void shouldResolveMarkdownEvenWithDotsInFilename() {
      ContentType contentType = contentTypeResolver.resolve("somedoc.1.1.md");
      assertThat(contentType.getLanguage()).contains("Markdown");
    }

    @Test
    void shouldResolveDockerfile() {
      ContentType contentType = contentTypeResolver.resolve("Dockerfile");
      assertThat(contentType.getLanguage()).contains("Dockerfile");
    }

  }

  @Nested
  class GetSyntaxModesTests {

    @Test
    void shouldReturnEmptyMapOfModesWithoutLanguage() {
      Map<String, String> syntaxModes = contentTypeResolver.resolve("app.exe").getSyntaxModes();
      assertThat(syntaxModes).isEmpty();
    }

    @Test
    void shouldReturnMapOfModes() {
      Map<String, String> syntaxModes = contentTypeResolver.resolve("app.rs").getSyntaxModes();
      assertThat(syntaxModes)
        .containsEntry("ace", "rust")
        .containsEntry("codemirror", "rust")
        .containsEntry("prism", "rust");
    }

  }

}
