package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  private boolean enableRepositoryArchive;
  private boolean disableGroupingGrid;
  private String dateFormat;
  private boolean anonymousAccessEnabled;
  private Set<String> adminGroups;
  private Set<String> adminUsers;
  private String baseUrl;
  private boolean forceBaseUrl;
  private int loginAttemptLimit;
  private Set<String> proxyExcludes;
  private boolean skipFailedAuthenticators;
  private String pluginUrl;
  private long loginAttemptLimitTimeout;
  private boolean enabledXsrfProtection;
  private String defaultNamespaceStrategy;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
