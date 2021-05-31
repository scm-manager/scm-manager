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

package sonia.scm.repository.spi;

import org.apache.commons.lang.StringUtils;
import sonia.scm.repository.api.Credential;
import sonia.scm.repository.api.MirrorFilter;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

/**
 * @since 2.19.0
 */
public final class MirrorCommandRequest {

  private String sourceUrl;
  private Collection<Credential> credentials = emptyList();
  private MirrorFilter filter = new MirrorFilter() {};

  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public Collection<Credential> getCredentials() {
    return unmodifiableCollection(credentials);
  }

  public <T extends Credential> Optional<T> getCredential(Class<T> credentialClass) {
    return getCredentials()
      .stream()
      .filter(credentialClass::isInstance)
      .map(credentialClass::cast)
      .findFirst();
  }

  public void setCredentials(Collection<Credential> credentials) {
    this.credentials = credentials;
  }

  public MirrorFilter getFilter() {
    return filter;
  }

  public void setFilter(MirrorFilter filter) {
    this.filter = filter;
  }

  public boolean isValid() {
    return StringUtils.isNotBlank(sourceUrl);
  }
}