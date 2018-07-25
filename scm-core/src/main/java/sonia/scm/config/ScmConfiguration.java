/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.config;


import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.util.HttpUtil;
import sonia.scm.xml.XmlSetStringAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//~--- JDK imports ------------------------------------------------------------

/**
 * The main configuration object for SCM-Manager.
 * This class is a singleton and is available via injection.
 *
 * @author Sebastian Sdorra
 */
@Singleton
@XmlRootElement(name = "scm-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmConfiguration {

  /**
   * Default JavaScript date format
   */
  public static final String DEFAULT_DATEFORMAT = "YYYY-MM-DD HH:mm:ss";

  /**
   * Default plugin url
   */
  public static final String DEFAULT_PLUGINURL =
    "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false";

  /**
   * Default plugin url from version 1.0
   */
  public static final String OLD_PLUGINURL =
    "http://plugins.scm-manager.org/plugins.xml.gz";

  /**
   * Path to the configuration file
   */
  public static final String PATH =
    "config".concat(File.separator).concat("config.xml");

  /**
   * the logger for ScmConfiguration
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmConfiguration.class);


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
  private String pluginUrl = DEFAULT_PLUGINURL;

  /**
   * Login attempt timeout.
   *
   * @since 1.34
   */
  @XmlElement(name = "login-attempt-limit-timeout")
  private long loginAttemptLimitTimeout = TimeUnit.MINUTES.toSeconds(5l);


  private boolean enableProxy = false;

  /**
   * Authentication realm for basic authentication.
   */
  private String realmDescription = HttpUtil.AUTHENTICATION_REALM;
  private boolean enableRepositoryArchive = false;
  private boolean disableGroupingGrid = false;
  /**
   * JavaScript date format from moment.js
   *
   * @see <a href="http://momentjs.com/docs/#/parsing/" target="_blank">http://momentjs.com/docs/#/parsing/</a>
   */
  private String dateFormat = DEFAULT_DATEFORMAT;
  private boolean anonymousAccessEnabled = false;

  /**
   * Enables xsrf cookie protection.
   *
   * @since 1.47
   */
  @XmlElement(name = "xsrf-protection")
  private boolean enabledXsrfProtection = true;

  @XmlElement(name = "default-namespace-strategy")
  private String defaultNamespaceStrategy = "sonia.scm.repository.DefaultNamespaceStrategy";


  /**
   * Calls the {@link sonia.scm.ConfigChangedListener#configChanged(Object)}
   * method of all registered listeners.
   */
  public void fireChangeEvent() {
    if (logger.isDebugEnabled()) {
      logger.debug("fire config changed event");
    }

    // fire event to event bus
    ScmEventBus.getInstance().post(new ScmConfigurationChangedEvent(this));
  }

  /**
   * Load all properties from another {@link ScmConfiguration} object.
   *
   * @param other
   */
  public void load(ScmConfiguration other) {
    this.realmDescription = other.realmDescription;
    this.dateFormat = other.dateFormat;
    this.pluginUrl = other.pluginUrl;
    this.anonymousAccessEnabled = other.anonymousAccessEnabled;
    this.adminUsers = other.adminUsers;
    this.adminGroups = other.adminGroups;
    this.enableProxy = other.enableProxy;
    this.proxyPort = other.proxyPort;
    this.proxyServer = other.proxyServer;
    this.proxyUser = other.proxyUser;
    this.proxyPassword = other.proxyPassword;
    this.proxyExcludes = other.proxyExcludes;
    this.forceBaseUrl = other.forceBaseUrl;
    this.baseUrl = other.baseUrl;
    this.disableGroupingGrid = other.disableGroupingGrid;
    this.enableRepositoryArchive = other.enableRepositoryArchive;
    this.skipFailedAuthenticators = other.skipFailedAuthenticators;
    this.loginAttemptLimit = other.loginAttemptLimit;
    this.loginAttemptLimitTimeout = other.loginAttemptLimitTimeout;
    this.enabledXsrfProtection = other.enabledXsrfProtection;
    this.defaultNamespaceStrategy = other.defaultNamespaceStrategy;
  }

  public Set<String> getAdminGroups() {
    return adminGroups;
  }

  public Set<String> getAdminUsers() {
    return adminUsers;
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
   * For example http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false
   *
   * @return the complete plugin url.
   */
  public String getPluginUrl() {
    return pluginUrl;
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

  public boolean isAnonymousAccessEnabled() {
    return anonymousAccessEnabled;
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

  public boolean isEnableProxy() {
    return enableProxy;
  }

  public boolean isEnableRepositoryArchive() {
    return enableRepositoryArchive;
  }

  public boolean isForceBaseUrl() {
    return forceBaseUrl;
  }

  public boolean isLoginAttemptLimitEnabled() {
    return loginAttemptLimit > 0;
  }

  public String getDefaultNamespaceStrategy() {
    return defaultNamespaceStrategy;
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

  public void setAdminGroups(Set<String> adminGroups) {
    this.adminGroups = adminGroups;
  }

  public void setAdminUsers(Set<String> adminUsers) {
    this.adminUsers = adminUsers;
  }

  public void setAnonymousAccessEnabled(boolean anonymousAccessEnabled) {
    this.anonymousAccessEnabled = anonymousAccessEnabled;
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

  /**
   * Enable or disable the repository archive. Default is disabled.
   *
   * @param enableRepositoryArchive true to disable the repository archive
   * @since 1.14
   */
  public void setEnableRepositoryArchive(boolean enableRepositoryArchive) {
    this.enableRepositoryArchive = enableRepositoryArchive;
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

  public void setDefaultNamespaceStrategy(String defaultNamespaceStrategy) {
    this.defaultNamespaceStrategy = defaultNamespaceStrategy;
  }
}
