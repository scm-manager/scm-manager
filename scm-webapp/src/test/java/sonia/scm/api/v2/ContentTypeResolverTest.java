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

package sonia.scm.api.v2;

import com.github.sdorra.spotter.ContentType;
import com.github.sdorra.spotter.Language;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ContentTypeResolverTest {

  @Test
  void shouldResolveMarkdown() {
    String content = String.join("\n",
      "% Markdown content",
      "% Which does not start with markdown"
    );
    ContentType contentType = ContentTypeResolver.resolve("somedoc.md", content.getBytes(StandardCharsets.UTF_8));
    assertThat(contentType.getLanguage()).contains(Language.MARKDOWN);
  }

  @Test
  void shouldResolveMarkdownWithoutContent() {
    ContentType contentType = ContentTypeResolver.resolve("somedoc.md");
    assertThat(contentType.getLanguage()).contains(Language.MARKDOWN);
  }

  @Test
  void shouldResolveMarkdownEvenWithDotsInFilename() {
    ContentType contentType = ContentTypeResolver.resolve("somedoc.1.1.md");
    assertThat(contentType.getLanguage()).contains(Language.MARKDOWN);
  }

  @Test
  void shouldResolveDockerfile() {
    ContentType contentType = ContentTypeResolver.resolve("Dockerfile");
    assertThat(contentType.getLanguage()).contains(Language.DOCKERFILE);
  }

}
