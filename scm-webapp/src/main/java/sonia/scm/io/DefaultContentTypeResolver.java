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

import com.cloudogu.spotter.ContentTypeDetector;
import com.cloudogu.spotter.Language;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public final class DefaultContentTypeResolver implements ContentTypeResolver {

  private static final Language[] BOOST = new Language[]{
    // GCC Machine Description uses .md as extension, but markdown is much more likely
    Language.MARKDOWN,
    // XML uses .rs as extension, but rust is much more likely
    Language.RUST,
    // XML is also returned by content type boost strategy, but rust is really much more likely
    Language.RUST,
  };

  private static final ContentTypeDetector PATH_BASED = ContentTypeDetector.builder()
    .defaultPathBased()
    .boost(BOOST)
    .bestEffortMatch();

  private static final ContentTypeDetector PATH_AND_CONTENT_BASED = ContentTypeDetector.builder()
    .defaultPathAndContentBased()
    .boost(BOOST)
    .bestEffortMatch();

  @Override
  public DefaultContentType resolve(String path) {
    return new DefaultContentType(PATH_BASED.detect(path));
  }

  @Override
  public DefaultContentType resolve(String path, byte[] contentPrefix) {
    return new DefaultContentType(PATH_AND_CONTENT_BASED.detect(path, contentPrefix));
  }

  @Override
  public Map<String, String> findSyntaxModesByLanguage(String language) {
    Optional<Language> byName = Language.getByName(language);
    if (byName.isPresent()) {
      return DefaultContentType.syntaxMode(byName.get());
    }
    return Collections.emptyMap();
  }
}
