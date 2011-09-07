/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
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
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.config;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigChangedListener;
import sonia.scm.ListenerSupport;
import sonia.scm.xml.XmlSetStringAdapter;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The main configuration object for SCM-Manager.
 * This class is a singleton and is available via injection.
 *
 * @author Sebastian Sdorra
 */
@Singleton
@XmlRootElement(name = "scm-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmConfiguration
        implements ListenerSupport<ConfigChangedListener<ScmConfiguration>>
{

  /** Default JavaScript date format */
  public static final String DEFAULT_DATEFORMAT = "Y-m-d H:i:s";

  /** Default plugin url */
  public static final String DEFAULT_PLUGINURL =
    "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false";

  /** Default plugin url from version 1.0 */
  public static final String OLD_PLUGINURL =
    "http://plugins.scm-manager.org/plugins.xml.gz";

  /** Path to the configuration file */
  public static final String PATH =
    "config".concat(File.separator).concat("config.xml");

  /** the logger for ScmConfiguration */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmConfiguration.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Register a {@link sonia.scm.ConfigChangedListener}
   *
   *
   *
   * @param listener
   */
  @Override
  public void addListener(ConfigChangedListener<ScmConfiguration> listener)
  {
    listeners.add(listener);
  }

  /**
   * Register a {@link java.util.Collection} of {@link sonia.scm.ConfigChangedListener}
   *
   *
   *
   * @param listeners
   */
  @Override
  public void addListeners(
          Collection<ConfigChangedListener<ScmConfiguration>> listeners)
  {
    listeners.addAll(listeners);
  }

  /**
   * Calls the {@link sonia.scm.ConfigChangedListener#configChanged(Object)}
   * method of all registered listeners.
   */
  public void fireChangeEvent()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("fire config changed event");
    }

    for (ConfigChangedListener listener : listeners)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("call listener {}", listener.getClass().getName());
      }

      listener.configChanged(this);
    }
  }

  /**
   * Load all properties from another {@link ScmConfiguration} object.
   *
   *
   *
   * @param other
   */
  public void load(ScmConfiguration other)
  {
    this.servername = other.servername;
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
    this.forceBaseUrl = other.forceBaseUrl;
    this.baseUrl = other.baseUrl;

    // deprecated fields
    this.sslPort = other.sslPort;
    this.enableSSL = other.enableSSL;
    this.enablePortForward = other.enablePortForward;
    this.forwardPort = other.forwardPort;
  }

  /**
   * Unregister a listener object.
   *
   *
   * @param listener
   */
  @Override
  public void removeListener(ConfigChangedListener listener)
  {
    listeners.remove(listener);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a set of admin group names.
   *
   *
   * @return set of admin group names
   */
  public Set<String> getAdminGroups()
  {
    return adminGroups;
  }

  /**
   * Returns a set of admin user names.
   *
   *
   * @return set of admin user names
   */
  public Set<String> getAdminUsers()
  {
    return adminUsers;
  }

  /**
   * Returns the complete base url of the scm-manager including the context path.
   * For example http://localhost:8080/scm
   *
   * @since 1.5
   * @return complete base url of the scm-manager
   */
  public String getBaseUrl()
  {
    return baseUrl;
  }

  /**
   * Returns the date format for the user interface. This format is a
   * JavaScript date format, see
   * {@link http://jacwright.com/projects/javascript/date_format}.
   *
   *
   * @return JavaScript date format
   */
  public String getDateFormat()
  {
    return dateFormat;
  }

  /**
   * Returns the forwarding port.
   *
   *
   * @return forwarding port
   * @deprecated use {@link #getBaseUrl()}
   */
  @Deprecated
  public int getForwardPort()
  {
    return forwardPort;
  }

  /**
   * Returns the url of the plugin repository. This url can contain placeholders.
   * Explanation of the {placeholders}:
   * <ul>
   * <li><b>version</b> = SCM-Manager Version</li>
   * <li><b>os</b> = Operation System</li>
   * <li><b>arch</b> = Architecture</li>
   * </ul>
   * For example {@link http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false"}
   *
   * @return the complete plugin url.
   */
  public String getPluginUrl()
  {
    return pluginUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.7
   */
  public String getProxyPassword()
  {
    return proxyPassword;
  }

  /**
   * Returns the proxy port.
   *
   *
   * @return proxy port
   */
  public int getProxyPort()
  {
    return proxyPort;
  }

  /**
   * Returns the servername or ip of the proxyserver.
   *
   *
   * @return servername or ip of the proxyserver
   */
  public String getProxyServer()
  {
    return proxyServer;
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.7
   */
  public String getProxyUser()
  {
    return proxyUser;
  }

  /**
   * Returns the servername of the SCM-Manager host.
   *
   *
   * @return servername of the SCM-Manager host
   * @deprecated use {@link #getBaseUrl()}
   */
  public String getServername()
  {
    return servername;
  }

  /**
   * Returns the ssl port.
   *
   *
   * @return ssl port
   * @deprecated use {@link #getBaseUrl()} and {@link #isForceBaseUrl()}
   */
  @Deprecated
  public int getSslPort()
  {
    return sslPort;
  }

  /**
   * Returns true if the anonymous access to the SCM-Manager is enabled.
   *
   *
   * @return true if the anonymous access to the SCM-Manager is enabled
   */
  public boolean isAnonymousAccessEnabled()
  {
    return anonymousAccessEnabled;
  }

  /**
   * Returns true if port forwarding is enabled.
   *
   *
   * @return true if port forwarding is enabled
   * @deprecated use {@link #getBaseUrl()}
   */
  @Deprecated
  public boolean isEnablePortForward()
  {
    return enablePortForward;
  }

  /**
   * Returns true if proxy is enabled.
   *
   *
   * @return true if proxy is enabled
   */
  public boolean isEnableProxy()
  {
    return enableProxy;
  }

  /**
   * Returns true if ssl is enabled.
   *
   *
   * @return true if ssl is enabled
   * @deprecated use {@link #getBaseUrl()} and {@link #isForceBaseUrl()}
   */
  @Deprecated
  public boolean isEnableSSL()
  {
    return enableSSL;
  }

  /**
   * Returns true if force base url is enabled.
   *
   * @since 1.5
   * @return true if force base url is enabled
   */
  public boolean isForceBaseUrl()
  {
    return forceBaseUrl;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param adminGroups
   */
  public void setAdminGroups(Set<String> adminGroups)
  {
    this.adminGroups = adminGroups;
  }

  /**
   * Method description
   *
   *
   * @param adminUsers
   */
  public void setAdminUsers(Set<String> adminUsers)
  {
    this.adminUsers = adminUsers;
  }

  /**
   * Method description
   *
   *
   * @param anonymousAccessEnabled
   */
  public void setAnonymousAccessEnabled(boolean anonymousAccessEnabled)
  {
    this.anonymousAccessEnabled = anonymousAccessEnabled;
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @since 1.5
   */
  public void setBaseUrl(String baseUrl)
  {
    this.baseUrl = baseUrl;
  }

  /**
   * Method description
   *
   *
   * @param dateFormat
   */
  public void setDateFormat(String dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  /**
   * Method description
   *
   *
   * @param enablePortForward
   * @deprecated use {@link #setBaseUrl(String)}
   */
  @Deprecated
  public void setEnablePortForward(boolean enablePortForward)
  {
    this.enablePortForward = enablePortForward;
  }

  /**
   * Method description
   *
   *
   * @param enableProxy
   */
  public void setEnableProxy(boolean enableProxy)
  {
    this.enableProxy = enableProxy;
  }

  /**
   * Method description
   *
   *
   * @param enableSSL
   * @deprecated use {@link #setBaseUrl(String)} and {$link #setForceBaseUrl(boolean)}
   */
  @Deprecated
  public void setEnableSSL(boolean enableSSL)
  {
    this.enableSSL = enableSSL;
  }

  /**
   * Method description
   *
   *
   * @param forceBaseUrl
   * @since 1.5
   */
  public void setForceBaseUrl(boolean forceBaseUrl)
  {
    this.forceBaseUrl = forceBaseUrl;
  }

  /**
   * Method description
   *
   *
   * @param forwardPort
   * @deprecated use {@link #setBaseUrl(String)}
   */
  @Deprecated
  public void setForwardPort(int forwardPort)
  {
    this.forwardPort = forwardPort;
  }

  /**
   * Method description
   *
   *
   * @param pluginUrl
   */
  public void setPluginUrl(String pluginUrl)
  {
    this.pluginUrl = pluginUrl;
  }

  /**
   * Method description
   *
   *
   * @param proxyPassword
   * @since 1.7
   */
  public void setProxyPassword(String proxyPassword)
  {
    this.proxyPassword = proxyPassword;
  }

  /**
   * Method description
   *
   *
   * @param proxyPort
   */
  public void setProxyPort(int proxyPort)
  {
    this.proxyPort = proxyPort;
  }

  /**
   * Method description
   *
   *
   * @param proxyServer
   */
  public void setProxyServer(String proxyServer)
  {
    this.proxyServer = proxyServer;
  }

  /**
   * Method description
   *
   *
   * @param proxyUser
   * @since 1.7
   */
  public void setProxyUser(String proxyUser)
  {
    this.proxyUser = proxyUser;
  }

  /**
   * Method description
   *
   *
   * @param servername
   * @deprecated use {@link #setBaseUrl(String)}
   */
  public void setServername(String servername)
  {
    this.servername = servername;
  }

  /**
   * Method description
   *
   *
   * @param sslPort
   * @deprecated use {@link #setBaseUrl(String)} and {@link #setForceBaseUrl(boolean)}
   */
  @Deprecated
  public void setSslPort(int sslPort)
  {
    this.sslPort = sslPort;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "admin-groups")
  @XmlJavaTypeAdapter(XmlSetStringAdapter.class)
  private Set<String> adminGroups;

  /** Field description */
  @XmlElement(name = "admin-users")
  @XmlJavaTypeAdapter(XmlSetStringAdapter.class)
  private Set<String> adminUsers;

  /** Field description */
  @XmlElement(name = "base-url")
  private String baseUrl;

  /** Field description */
  private boolean enableProxy = false;

  /** Field description */
  @XmlElement(name = "force-base-url")
  private boolean forceBaseUrl;

  /** @deprecated use {@link #baseUrl} */
  @Deprecated
  private int forwardPort = 80;

  /** Field description */
  @XmlElement(name = "plugin-url")
  private String pluginUrl = DEFAULT_PLUGINURL;

  /** Field description */
  private String proxyPassword;

  /** Field description */
  private int proxyPort = 8080;

  /** Field description */
  private String proxyServer = "proxy.mydomain.com";

  /** Field description */
  private String proxyUser;

  /** @deprecated use {@link #baseUrl} */
  private String servername = "localhost";

  /** @deprecated use {@link #baseUrl} and {@link #forceBaseUrl} */
  @Deprecated
  private boolean enableSSL = false;

  /** @deprecated use {@link #baseUrl} */
  @Deprecated
  private boolean enablePortForward = false;

  /** @deprecated use {@link #baseUrl} and {@link #forceBaseUrl} */
  @Deprecated
  private int sslPort = 8181;

  /** Configuration change listeners */
  @XmlTransient
  private Set<ConfigChangedListener> listeners =
    new HashSet<ConfigChangedListener>();

  /**
   * JavaScript date format, see http://jacwright.com/projects/javascript/date_format
   */
  private String dateFormat = DEFAULT_DATEFORMAT;

  /** Field description */
  private boolean anonymousAccessEnabled = false;
}
