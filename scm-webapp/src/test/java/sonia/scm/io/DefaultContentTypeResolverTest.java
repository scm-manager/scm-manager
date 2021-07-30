/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.io;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefaultContentTypeResolverTest {

  private final DefaultContentTypeResolver contentTypeResolver = new DefaultContentTypeResolver();

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

  @Nested
  class IsTextTests {

    @ParameterizedTest(name = "shouldReturnIsTextFor{argumentsWithNames}")
    @ValueSource(strings = {"App.java", "Dockerfile", "Playbook.yml", "README.md", "LICENSE.txt"})
    void shouldReturnIsTextFor(String path) {
      ContentType contentType = contentTypeResolver.resolve(path);
      assertThat(contentType.isText()).isTrue();
    }

    @ParameterizedTest(name = "shouldReturnIsNotTextFor{argumentsWithNames}")
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
      Assertions.assertThat(contentType.getLanguage()).contains("markdown");
    }

    @Test
    void shouldResolveMarkdownWithoutContent() {
      ContentType contentType = contentTypeResolver.resolve("somedoc.md");
      Assertions.assertThat(contentType.getLanguage()).contains("markdown");
    }

    @Test
    void shouldResolveMarkdownEvenWithDotsInFilename() {
      ContentType contentType = contentTypeResolver.resolve("somedoc.1.1.md");
      Assertions.assertThat(contentType.getLanguage()).contains("markdown");
    }

    @Test
    void shouldResolveDockerfile() {
      ContentType contentType = contentTypeResolver.resolve("Dockerfile");
      Assertions.assertThat(contentType.getLanguage()).contains("dockerfile");
    }


    @Test
    void shouldReturnAceModeIfPresent() {
      assertThat(contentTypeResolver.resolve("app.go").getLanguage()).contains("golang"); // codemirror is just go
      assertThat(contentTypeResolver.resolve("App.java").getLanguage()).contains("java"); // codemirror is clike
    }

    @Test
    void shouldReturnCodemirrorIfAceModeIsMissing() {
      assertThat(contentTypeResolver.resolve("index.ecr").getLanguage()).contains("htmlmixed");
    }

    @Test
    void shouldReturnTextIfNoModeIsPresent() {
      assertThat(contentTypeResolver.resolve("index.hxml").getLanguage()).contains("text");
    }

  }

}
