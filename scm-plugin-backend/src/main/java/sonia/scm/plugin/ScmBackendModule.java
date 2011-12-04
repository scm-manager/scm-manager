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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import sonia.scm.ConfigurationException;
import sonia.scm.plugin.rest.url.BitbucketUrlBuilder;
import sonia.scm.plugin.rest.url.UrlBuilder;
import sonia.scm.plugin.rest.url.UrlBuilderFactory;
import sonia.scm.plugin.rest.url.GithubUrlBuilder;
import sonia.scm.plugin.scanner.DefaultPluginScannerFactory;
import sonia.scm.plugin.scanner.PluginScannerFactory;
import sonia.scm.plugin.scanner.PluginScannerScheduler;
import sonia.scm.plugin.scanner.TimerPluginScannerScheduler;
import sonia.scm.util.Util;
import sonia.scm.web.proxy.ProxyServet;
import sonia.scm.web.proxy.ProxyURLProvider;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmBackendModule extends ServletModule
{

  /** Field description */
  public static final String DIRECTORY_DEFAULT = ".scm-backend";

  /** Field description */
  public static final String DIRECTORY_ENVIRONMENT = "SCMBACKEND_HOME";

  /** Field description */
  public static final String DIRECTORY_PROPERTY = "scm-backend.home";

  /** Field description */
  public static final String FILE_CONFIG = "config.xml";

  /** Field description */
  public static final String PATTERN_API = "/api/*";

  /** Field description */
  public static final String PATTERN_PAGE = "/page/*";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    File baseDirectory = findBaseDirectory();

    if (baseDirectory == null)
    {
      throw new ConfigurationException("could not find base directory");
    }

    File configurationFile = getConfigurationFile(baseDirectory);

    if (!configurationFile.exists())
    {
      throw new ConfigurationException(
          "could not find configuration at ".concat(
            configurationFile.getPath()));
    }

    BackendConfiguration configuration = JAXB.unmarshal(configurationFile,
                                           BackendConfiguration.class);

    bind(File.class).annotatedWith(Names.named(DIRECTORY_PROPERTY)).toInstance(
        baseDirectory);
    bind(BackendConfiguration.class).toInstance(configuration);
    bind(PluginBackend.class).to(DefaultPluginBackend.class);
    bind(PluginScannerFactory.class).to(DefaultPluginScannerFactory.class);
    bind(PluginScannerScheduler.class).to(TimerPluginScannerScheduler.class);

    // compare url builder
    Multibinder<UrlBuilder> compareUrlBuilderBinder =
      Multibinder.newSetBinder(binder(), UrlBuilder.class);

    compareUrlBuilderBinder.addBinding().to(BitbucketUrlBuilder.class);
    compareUrlBuilderBinder.addBinding().to(GithubUrlBuilder.class);

    // compare url builder factory
    bind(UrlBuilderFactory.class);

    // news proxy
    bind(ProxyURLProvider.class).to(NewsProxyURLProvider.class);
    serve("/news*").with(ProxyServet.class);

    Map<String, String> params = new HashMap<String, String>();

    params.put(PackagesResourceConfig.PROPERTY_PACKAGES,
               "sonia.scm.plugin.rest");
    serve(PATTERN_API, PATTERN_PAGE).with(GuiceContainer.class, params);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private File findBaseDirectory()
  {
    String path = System.getProperty(DIRECTORY_PROPERTY);

    if (Util.isEmpty(path))
    {
      path = System.getenv(DIRECTORY_ENVIRONMENT);

      if (Util.isEmpty(path))
      {
        path = System.getProperty("user.home").concat(File.separator).concat(
          DIRECTORY_DEFAULT);
      }
    }

    File directory = new File(path);

    if (!directory.exists() &&!directory.mkdirs())
    {
      throw new IllegalStateException("could not create directory");
    }

    return directory;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseDirectory
   *
   * @return
   */
  private File getConfigurationFile(File baseDirectory)
  {
    return new File(baseDirectory, FILE_CONFIG);
  }
}
