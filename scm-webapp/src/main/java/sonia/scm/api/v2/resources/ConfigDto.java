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
    
package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.security.AnonymousMode;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class ConfigDto extends HalRepresentation {

  private String proxyPassword;
  private int proxyPort;
  private String proxyServer;
  private String proxyUser;
  private boolean enableProxy;
  private String realmDescription;
  private boolean disableGroupingGrid;
  private String dateFormat;
  private boolean anonymousAccessEnabled;
  private AnonymousMode anonymousMode;
  private String baseUrl;
  private boolean forceBaseUrl;
  private int loginAttemptLimit;
  private Set<String> proxyExcludes;
  private boolean skipFailedAuthenticators;
  private String pluginUrl;
  private long loginAttemptLimitTimeout;
  private boolean enabledXsrfProtection;
  private String namespaceStrategy;
  private String loginInfoUrl;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
