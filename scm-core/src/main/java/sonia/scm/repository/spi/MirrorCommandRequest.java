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

package sonia.scm.repository.spi;

import com.google.common.annotations.Beta;
import jakarta.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import sonia.scm.net.ProxyConfiguration;
import sonia.scm.repository.api.Credential;
import sonia.scm.repository.api.MirrorFilter;
import sonia.scm.security.PublicKey;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

/**
 * @since 2.19.0
 */
@Beta
public final class MirrorCommandRequest {

  private String sourceUrl;
  private Collection<Credential> credentials = emptyList();
  private List<PublicKey> publicKeys = emptyList();
  private MirrorFilter filter = new MirrorFilter() {};

  @Nullable
  private ProxyConfiguration proxyConfiguration;
  private boolean ignoreLfs;

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

  public void setIgnoreLfs(boolean ignoreLfs) {
    this.ignoreLfs = ignoreLfs;
  }

  public boolean isIgnoreLfs() {
    return ignoreLfs;
  }

  public boolean isValid() {
    return StringUtils.isNotBlank(sourceUrl);
  }

  public void setPublicKeys(List<PublicKey> publicKeys) {
    this.publicKeys = publicKeys;
  }

  public List<PublicKey> getPublicKeys() {
    return Collections.unmodifiableList(publicKeys);
  }

  /**
   * Use the provided proxy configuration for the connection to the source repository.
   * @param proxyConfiguration proxy configuration
   * @since 2.23.0
   */
  public void setProxyConfiguration(ProxyConfiguration proxyConfiguration) {
    this.proxyConfiguration = proxyConfiguration;
  }

  /**
   * Returns an optional proxy configuration which is used for the connection to the source repository.
   * @return optional proxy configuration or empty
   * @since 2.23.0
   */
  public Optional<ProxyConfiguration> getProxyConfiguration() {
    return Optional.ofNullable(proxyConfiguration);
  }
}
