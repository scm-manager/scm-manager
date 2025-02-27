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

package sonia.scm.config;


import com.google.common.collect.Sets;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.security.AnonymousMode;
import sonia.scm.util.HttpUtil;
import sonia.scm.xml.XmlCipherStringAdapter;
import sonia.scm.xml.XmlSetStringAdapter;

import java.io.File;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The main configuration object for SCM-Manager.
 *
 */

@Singleton
@XmlRootElement(name = "scm-config")
@XmlAccessorType(XmlAccessType.FIELD)
@AuditEntry(labels = {"general","config"}, maskedFields = "proxyPassword")
public class ScmConfiguration implements Configuration {

  /**
   * Default JavaScript date format
   */
  public static final String DEFAULT_DATEFORMAT = "YYYY-MM-DD HH:mm:ss";

  public static final String DEFAULT_PLUGIN_URL =
    "https://plugin-center-api.scm-manager.org/api/v1/plugins/{version}?os={os}&arch={arch}&jre={jre}";

  /**
   * Default url for plugin center authentication.
   *
   * @since 2.28.0
   */
  public static final String DEFAULT_PLUGIN_AUTH_URL = "https://plugin-center-api.scm-manager.org/api/v1/auth/oidc";

  /**
   * SCM Manager alerts url.
   *
   * @since 2.30.0
   */
  public static final String DEFAULT_ALERTS_URL = "https://alerts.scm-manager.org/api/v1/alerts";

  public static final String DEFAULT_RELEASE_FEED_URL = "https://scm-manager.org/download/rss.xml";

  /**
   * Default url for login information (plugin and feature tips on the login page).
   */
  public static final String DEFAULT_LOGIN_INFO_URL = "https://login-info.scm-manager.org/api/v1/login-info";

  /**
   * Default e-mail domain name that will be used whenever we have to generate an e-mail address for a user that has no
   * mail address configured.
   *
   * @since 2.8.0
   */
  public static final String DEFAULT_MAIL_DOMAIN_NAME = "scm-manager.local";

  /**
   * Default plugin url from version 1.0
   */
  public static final String OLD_PLUGINURL =
    "http://plugins.scm-manager.org/plugins.xml.gz";

  /**
   * Path to the configuration file
   */
  public static final String PATH = "config".concat(File.separator).concat("config.xml");

  private static final Logger LOG = LoggerFactory.getLogger(ScmConfiguration.class);

  @SuppressWarnings("WeakerAccess") // This might be needed for permission checking
  public static final String PERMISSION = "global";

  @XmlElement(name = "base-url")
  private String baseUrl;


  @XmlElement(name = "force-base-url")
  private boolean forceBaseUrl;

  /**
   * Maximum allowed login attempts.
   *
   * @since 1.34
   */
  @XmlElement(name = "login-attempt-limit")
  private int loginAttemptLimit = -1;

  /**
   * glob patterns for urls which are excluded from proxy
   */
  @XmlElement(name = "proxy-excludes")
  @XmlJavaTypeAdapter(XmlSetStringAdapter.class)
  private Set<String> proxyExcludes;


  @XmlJavaTypeAdapter(XmlCipherStringAdapter.class)
  private String proxyPassword;


  private int proxyPort = 8080;


  private String proxyServer = "proxy.mydomain.com";


  private String proxyUser;

  /**
   * Skip failed authenticators.
   *
   * @since 1.36
   */
  @XmlElement(name = "skip-failed-authenticators")
  private boolean skipFailedAuthenticators = false;


  @XmlElement(name = "plugin-url")
  private String pluginUrl = DEFAULT_PLUGIN_URL;

  @XmlElement(name = "plugin-auth-url")
  private String pluginAuthUrl = DEFAULT_PLUGIN_AUTH_URL;

  /**
   * Url of the alerts api.
   *
   * @since 2.30.0
   */
  @XmlElement(name = "alerts-url")
  private String alertsUrl = DEFAULT_ALERTS_URL;

  @XmlElement(name = "release-feed-url")
  private String releaseFeedUrl = DEFAULT_RELEASE_FEED_URL;

  /**
   * Login attempt timeout.
   *
   * @since 1.34
   */
  @XmlElement(name = "login-attempt-limit-timeout")
  private long loginAttemptLimitTimeout = TimeUnit.MINUTES.toSeconds(5L);


  private boolean enableProxy = false;

  /**
   * Authentication realm for basic authentication.
   */
  private String realmDescription = HttpUtil.AUTHENTICATION_REALM;
  private boolean disableGroupingGrid = false;
  /**
   * JavaScript date format from moment.js
   *
   * @see <a href="http://momentjs.com/docs/#/parsing/" target="_blank">http://momentjs.com/docs/#/parsing/</a>
   */
  private String dateFormat = DEFAULT_DATEFORMAT;
  private AnonymousMode anonymousMode = AnonymousMode.OFF;

  /**
   * Enables xsrf cookie protection.
   *
   * @since 1.47
   */
  @XmlElement(name = "xsrf-protection")
  private boolean enabledXsrfProtection = true;

  /**
   * Enables user converter.
   *
   * @since 2.9.0
   */
  @XmlElement(name = "user-converter")
  private boolean enabledUserConverter = false;

  /**
   * Enables api keys for all users.
   *
   * @since 2.16.0
   */
  @XmlElement(name = "api-keys")
  private boolean enabledApiKeys = true;

  /**
   * Enables file search inside repositories.
   *
   * @since 2.45.0
   */
  @XmlElement(name = "file-search")
  private boolean enabledFileSearch = true;

  @XmlElement(name = "namespace-strategy")
  private String namespaceStrategy = "UsernameNamespaceStrategy";

  @XmlElement(name = "login-info-url")
  private String loginInfoUrl = DEFAULT_LOGIN_INFO_URL;

  @XmlElement(name = "mail-domain-name")
  private String mailDomainName = DEFAULT_MAIL_DOMAIN_NAME;

  /**
   * Time in hours for jwt expiration.
   *
   * @since 3.8.0
   */
  @XmlElement(name = "jwt-expiration-time")
  private int jwtExpirationInH = 1;

  /**
   * Enables endless jwt.
   *
   * @since 3.8.0
   */
  @XmlElement(name = "jwt-expiration-endless")
  private boolean enabledJwtEndless = false;

  /**
   * List of users that will be notified of administrative incidents.
   *
   * @since 2.19.0
   */
  @XmlElement(name = "emergency-contacts")
  private Set<String> emergencyContacts;

  public void fireChangeEvent() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("fire config changed event");
    }

    // fire event to event bus
    ScmEventBus.getInstance().post(new ScmConfigurationChangedEvent(this));
  }

  /**
   * Load all properties from another {@link ScmConfiguration} object.
   *
   * @param other {@link ScmConfiguration} to load from
   */
  public void load(ScmConfiguration other) {
    this.realmDescription = other.realmDescription;
    this.dateFormat = other.dateFormat;
    this.pluginUrl = other.pluginUrl;
    this.pluginAuthUrl = other.pluginAuthUrl;
    this.anonymousMode = other.anonymousMode;
    this.enableProxy = other.enableProxy;
    this.proxyPort = other.proxyPort;
    this.proxyServer = other.proxyServer;
    this.proxyUser = other.proxyUser;
    this.proxyPassword = other.proxyPassword;
    this.proxyExcludes = other.proxyExcludes;
    this.forceBaseUrl = other.forceBaseUrl;
    this.baseUrl = other.baseUrl;
    this.disableGroupingGrid = other.disableGroupingGrid;
    this.skipFailedAuthenticators = other.skipFailedAuthenticators;
    this.loginAttemptLimit = other.loginAttemptLimit;
    this.loginAttemptLimitTimeout = other.loginAttemptLimitTimeout;
    this.enabledXsrfProtection = other.enabledXsrfProtection;
    this.namespaceStrategy = other.namespaceStrategy;
    this.loginInfoUrl = other.loginInfoUrl;
    this.alertsUrl = other.alertsUrl;
    this.releaseFeedUrl = other.releaseFeedUrl;
    this.mailDomainName = other.mailDomainName;
    this.emergencyContacts = other.emergencyContacts;
    this.enabledUserConverter = other.enabledUserConverter;
    this.enabledApiKeys = other.enabledApiKeys;
    this.jwtExpirationInH = other.jwtExpirationInH;
    this.enabledJwtEndless = other.enabledJwtEndless;
  }

  /**
   * Returns the complete base url of the scm-manager including the context path.
   * For example http://localhost:8080/scm
   *
   * @return complete base url of the scm-manager
   * @since 1.5
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Returns the context path from the base url (see {@link #getBaseUrl()}) without starting or ending slashes.
   *
   * @since 2.42.0
   */
  public String getServerContextPath() {
    String path = URI.create(getBaseUrl()).getPath();
    if (path.startsWith("/") && path.length() > 1) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    }
    return path;
  }

  /**
   * Returns the date format for the user interface. This format is a
   * JavaScript date format, from the library moment.js.
   *
   * @return moment.js date format
   * @see <a href="http://momentjs.com/docs/#/parsing/" target="_blank">http://momentjs.com/docs/#/parsing/</a>
   */
  public String getDateFormat() {
    return dateFormat;
  }

  public int getLoginAttemptLimit() {
    return loginAttemptLimit;
  }

  /**
   * Returns the timeout in seconds for users which are temporary disabled,
   * because of too many failed login attempts.
   *
   * @return login attempt timeout in seconds
   * @since 1.34
   */
  public long getLoginAttemptLimitTimeout() {
    return loginAttemptLimitTimeout;
  }

  /**
   * Returns the url of the plugin repository. This url can contain placeholders.
   * Explanation of the {placeholders}:
   * <ul>
   * <li><b>version</b> = SCM-Manager Version</li>
   * <li><b>os</b> = Operation System</li>
   * <li><b>arch</b> = Architecture</li>
   * </ul>
   * For example `http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&amp;arch={arch}&amp;snapshot=false`
   *
   * @return the complete plugin url.
   */
  public String getPluginUrl() {
    return pluginUrl;
  }

  /**
   * Returns the url which is used for plugin center authentication.
   *
   * @return authentication url
   * @since 2.28.0
   */
  public String getPluginAuthUrl() {
    return pluginAuthUrl;
  }

  /**
   * Returns {@code true} if the default plugin auth url is used.
   *
   * @return {@code true} if the default plugin auth url is used
   * @since 2.28.0
   */
  public boolean isDefaultPluginAuthUrl() {
    return DEFAULT_PLUGIN_AUTH_URL.equals(pluginAuthUrl);
  }

  /**
   * Returns the url of the alerts api.
   *
   * @return the alerts url.
   * @since 2.30.0
   */
  public String getAlertsUrl() {
    return alertsUrl;
  }

  /**
   * Returns the url of the rss release feed.
   *
   * @return the rss release feed url.
   */
  public String getReleaseFeedUrl() {
    return releaseFeedUrl;
  }

  /**
   * Returns the mail domain, that will be used to create e-mail addresses for users without one whenever one is required.
   *
   * @return default mail domain
   * @since 2.8.0
   */
  public String getMailDomainName() {
    return mailDomainName;
  }

  /**
   * Returns a set of glob patterns for urls which should excluded from
   * proxy settings.
   *
   * @return set of glob patterns
   * @since 1.23
   */
  public Set<String> getProxyExcludes() {
    if (proxyExcludes == null) {
      proxyExcludes = Sets.newHashSet();
    }

    return proxyExcludes;
  }

  public String getProxyPassword() {
    return proxyPassword;
  }

  public int getProxyPort() {
    return proxyPort;
  }

  /**
   * Returns the servername or ip of the proxyserver.
   *
   * @return servername or ip of the proxyserver
   */
  public String getProxyServer() {
    return proxyServer;
  }

  public String getProxyUser() {
    return proxyUser;
  }

  public String getRealmDescription() {
    return realmDescription;
  }

  /**
   * Returns the currently enabled type of anonymous mode.
   *
   * @return anonymous mode
   * @since 2.4.0
   */
  public AnonymousMode getAnonymousMode() {
    return anonymousMode;
  }

  /**
   * Returns Jwt expiration in {@code n} .
   *
   * @return Jwt expiration in {@code number}
   * @since 3.8.0
   */
  public int getJwtExpirationInH() {
    return jwtExpirationInH;
  }

  /**
   * Returns {@code true} if the cookie xsrf protection is enabled.
   *
   * @return {@code true} if the cookie xsrf protection is enabled
   * @since 3.8.0
   */
  public boolean isJwtEndless() {
    return enabledJwtEndless;
  }

  /**
   * Returns {@code true} if anonymous mode is enabled.
   *
   * @return {@code true} if anonymous mode is enabled
   * @deprecated since 2.4.0 use {@link ScmConfiguration#getAnonymousMode} instead
   */
  @Deprecated
  public boolean isAnonymousAccessEnabled() {
    return anonymousMode != AnonymousMode.OFF;
  }

  public boolean isDisableGroupingGrid() {
    return disableGroupingGrid;
  }

  /**
   * Returns {@code true} if the cookie xsrf protection is enabled.
   *
   * @return {@code true} if the cookie xsrf protection is enabled
   * @see <a href="https://goo.gl/s67xO3">Issue 793</a>
   * @since 1.47
   */
  public boolean isEnabledXsrfProtection() {
    return enabledXsrfProtection;
  }

  /**
   * Returns {@code true} if the user converter is enabled.
   *
   * @return {@code true} if the user converter is enabled
   * The user converter automatically converts an internal user to external on their first login using an external system like ldap
   * @since 2.9.0
   */
  public boolean isEnabledUserConverter() {
    return enabledUserConverter;
  }

  /**
   * Returns {@code true} if the api keys are enabled.
   *
   * @return {@code true} if the api keys is enabled
   * @since 2.16.0
   */
  public boolean isEnabledApiKeys() {
    return enabledApiKeys;
  }

  public boolean isEnableProxy() {
    return enableProxy;
  }

  /**
   * Returns {@code true} if the repository file search is enabled.
   *
   * @return {@code true} if the api keys is enabled
   * @since 2.45.0
   */
  public boolean isEnabledFileSearch() { return enabledFileSearch; }

  public boolean isForceBaseUrl() {
    return forceBaseUrl;
  }

  public boolean isLoginAttemptLimitEnabled() {
    return loginAttemptLimit > 0;
  }

  public String getNamespaceStrategy() {
    return namespaceStrategy;
  }

  public String getLoginInfoUrl() {
    return loginInfoUrl;
  }

  /**
   * Returns true if failed authenticators are skipped.
   *
   * @return true if failed authenticators are skipped
   * @since 1.36
   */
  public boolean isSkipFailedAuthenticators() {
    return skipFailedAuthenticators;
  }

  public Set<String> getEmergencyContacts() {
    if (emergencyContacts == null) {
      emergencyContacts = Sets.newHashSet();
    }

    return emergencyContacts;
  }

  /**
   * Enables the anonymous access at protocol level.
   *
   * @param anonymousAccessEnabled enable or disables the anonymous access
   * @deprecated since 2.4.0 use {@link ScmConfiguration#setAnonymousMode(AnonymousMode)} instead
   */
  @Deprecated
  public void setAnonymousAccessEnabled(boolean anonymousAccessEnabled) {
    if (anonymousAccessEnabled) {
      this.anonymousMode = AnonymousMode.PROTOCOL_ONLY;
    } else {
      this.anonymousMode = AnonymousMode.OFF;
    }
  }

  /**
   * Configures the anonymous mode.
   *
   * @param mode type of anonymous mode
   * @since 2.4.0
   */
  public void setAnonymousMode(AnonymousMode mode) {
    this.anonymousMode = mode;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }

  public void setDisableGroupingGrid(boolean disableGroupingGrid) {
    this.disableGroupingGrid = disableGroupingGrid;
  }

  public void setEnableProxy(boolean enableProxy) {
    this.enableProxy = enableProxy;
  }

  public void setForceBaseUrl(boolean forceBaseUrl) {
    this.forceBaseUrl = forceBaseUrl;
  }

  /**
   * Set maximum allowed login attempts.
   *
   * @param loginAttemptLimit login attempt limit
   * @since 1.34
   */
  public void setLoginAttemptLimit(int loginAttemptLimit) {
    this.loginAttemptLimit = loginAttemptLimit;
  }

  /**
   * Sets the timeout in seconds for users which are temporary disabled,
   * because of too many failed login attempts.
   *
   * @param loginAttemptLimitTimeout login attempt timeout in seconds
   * @since 1.34
   */
  public void setLoginAttemptLimitTimeout(long loginAttemptLimitTimeout) {
    this.loginAttemptLimitTimeout = loginAttemptLimitTimeout;
  }

  public void setPluginUrl(String pluginUrl) {
    this.pluginUrl = pluginUrl;
  }

  /**
   * Set the url for plugin center authentication.
   *
   * @param pluginAuthUrl authentication url
   * @since 2.28.0
   */
  public void setPluginAuthUrl(String pluginAuthUrl) {
    this.pluginAuthUrl = pluginAuthUrl;
  }

  /**
   * Set the url for the alerts api.
   *
   * @param alertsUrl alerts url
   * @since 2.30.0
   */
  public void setAlertsUrl(String alertsUrl) {
    this.alertsUrl = alertsUrl;
  }

  public void setReleaseFeedUrl(String releaseFeedUrl) {
    this.releaseFeedUrl = releaseFeedUrl;
  }

  /**
   * Sets the mail host, that will be used to create e-mail addresses for users without one whenever one is required.
   *
   * @param mailDomainName The default mail domain to use
   * @since 2.8.0
   */
  public void setMailDomainName(String mailDomainName) {
    this.mailDomainName = mailDomainName;
  }

  /**
   * Set glob patterns for urls which are should be excluded from proxy
   * settings.
   *
   * @param proxyExcludes glob patterns
   * @since 1.23
   */
  public void setProxyExcludes(Set<String> proxyExcludes) {
    this.proxyExcludes = proxyExcludes;
  }

  public void setProxyPassword(String proxyPassword) {
    this.proxyPassword = proxyPassword;
  }

  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public void setProxyServer(String proxyServer) {
    this.proxyServer = proxyServer;
  }

  public void setProxyUser(String proxyUser) {
    this.proxyUser = proxyUser;
  }

  public void setRealmDescription(String realmDescription) {
    this.realmDescription = realmDescription;
  }

  /**
   * If set to true the authentication chain is not stopped, if an
   * authenticator finds the user but fails to authenticate the user.
   *
   * @param skipFailedAuthenticators true to skip failed authenticators
   * @since 1.36
   */
  public void setSkipFailedAuthenticators(boolean skipFailedAuthenticators) {
    this.skipFailedAuthenticators = skipFailedAuthenticators;
  }

  /**
   * Set {@code true} to enable xsrf cookie protection.
   *
   * @param enabledXsrfProtection {@code true} to enable xsrf protection
   * @see <a href="https://goo.gl/s67xO3">Issue 793</a>
   * @since 1.47
   */
  public void setEnabledXsrfProtection(boolean enabledXsrfProtection) {
    this.enabledXsrfProtection = enabledXsrfProtection;
  }

  /**
   * Set {@code true} to enable user converter.
   *
   * @param enabledUserConverter {@code true} to enable user converter
   * @since 2.9.0
   */
  public void setEnabledUserConverter(boolean enabledUserConverter) {
    this.enabledUserConverter = enabledUserConverter;
  }

  /**
   * Set {@code true} to enable api keys.
   *
   * @param enabledApiKeys {@code true} to enable api keys
   * @since 2.16.0
   */
  public void setEnabledApiKeys(boolean enabledApiKeys) {
    this.enabledApiKeys = enabledApiKeys;
  }

  /**
   * Set {@code true} to enable file search for repositories.
   *
   * @param enabledFileSearch {@code true} to enable file search for repositories
   * @since 2.45.0
   */
  public void setEnabledFileSearch(boolean enabledFileSearch) {
    this.enabledFileSearch = enabledFileSearch;
  }

  /**
   * Set {@code n} to configure jwt expiration time in hours
   *
   * @param jwtExpirationInH {@code n} to configure jwt expiration time in hours
   * @since 3.8.0
   */
  public void setJwtExpirationInH(int jwtExpirationInH) {
    this.jwtExpirationInH = jwtExpirationInH;
  }

  /**
   * Set {@code true} to enable endless jwt.
   *
   * @param enabledJwtEndless {@code true} to enable endless jwt.
   * @since 2.45.0
   */
  public void setEnabledJwtExpiration(boolean enabledJwtEndless) {
    this.enabledJwtEndless = enabledJwtEndless;
  }

  public void setNamespaceStrategy(String namespaceStrategy) {
    this.namespaceStrategy = namespaceStrategy;
  }

  public void setLoginInfoUrl(String loginInfoUrl) {
    this.loginInfoUrl = loginInfoUrl;
  }

  public void setEmergencyContacts(Set<String> emergencyContacts) {
    this.emergencyContacts = emergencyContacts;
  }

  @Override
  // Only for permission checks, don't serialize to XML
  @XmlTransient
  public String getId() {
    // Don't change this without migrating SCM permission configuration!
    return PERMISSION;
  }
}
