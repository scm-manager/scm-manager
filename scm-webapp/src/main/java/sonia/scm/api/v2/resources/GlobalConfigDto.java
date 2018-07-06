package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.xml.XmlSetStringAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class GlobalConfigDto extends HalRepresentation {

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
  @XmlElement(name = "admin-groups")
  @XmlJavaTypeAdapter(XmlSetStringAdapter.class)
  private Set<String> adminGroups;
  @XmlElement(name = "admin-users")
  @XmlJavaTypeAdapter(XmlSetStringAdapter.class)
  private Set<String> adminUsers;
  @XmlElement(name = "base-url")
  private String baseUrl;
  @XmlElement(name = "force-base-url")
  private boolean forceBaseUrl;
  @XmlElement(name = "login-attempt-limit")
  private int loginAttemptLimit;
  @XmlElement(name = "proxy-excludes")
  @XmlJavaTypeAdapter(XmlSetStringAdapter.class)
  private Set<String> proxyExcludes;
  @XmlElement(name = "skip-failed-authenticators")
  private boolean skipFailedAuthenticators;
  @XmlElement(name = "plugin-url")
  private String pluginUrl;
  @XmlElement(name = "login-attempt-limit-timeout")
  private long loginAttemptLimitTimeout;
  @XmlElement(name = "xsrf-protection")
  private boolean enabledXsrfProtection;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
