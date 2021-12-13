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

import com.cloudogu.spotter.Language;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class DefaultContentType implements ContentType {

  private final com.cloudogu.spotter.ContentType contentType;

  DefaultContentType(com.cloudogu.spotter.ContentType contentType) {
    this.contentType = contentType;
  }

  @Override
  public String getPrimary() {
    return contentType.getPrimary();
  }

  @Override
  public String getSecondary() {
    return contentType.getSecondary();
  }

  @Override
  public String getRaw() {
    return contentType.getRaw();
  }

  @Override
  public boolean isText() {
    return contentType.isText();
  }

  @Override
  public Optional<String> getLanguage() {
    return contentType.getLanguage().map(Language::getName);
  }

  @Override
  public Map<String, String> getSyntaxModes() {
    Optional<Language> language = contentType.getLanguage();
    if (language.isPresent()) {
      return syntaxMode(language.get());
    }
    return Collections.emptyMap();
  }

  static Map<String, String> syntaxMode(Language language) {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    language.getAceMode().ifPresent(mode -> builder.put("ace", mode));
    language.getCodemirrorMode().ifPresent(mode -> builder.put("codemirror", mode));
    language.getPrismMode().ifPresent(mode -> builder.put("prism", mode));
    return builder.build();
  }
}
