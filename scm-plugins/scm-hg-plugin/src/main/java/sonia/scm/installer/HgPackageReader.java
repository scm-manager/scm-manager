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



package sonia.scm.installer;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.PlatformType;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.net.HttpClient;
import sonia.scm.net.HttpRequest;
import sonia.scm.net.HttpResponse;
import sonia.scm.util.IOUtil;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgPackageReader
{

  /** Field description */
  public static final String CACHENAME = "sonia.scm.hg.packages";

  /** Field description */
  public static final String PACKAGEURL =
    "http://download.scm-manager.org/pkg/mercurial/packages.xml.gz";

  /** the logger for HgPackageReader */
  private static final Logger logger =
    LoggerFactory.getLogger(HgPackageReader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param cacheManager
   * @param httpClientProvider
   */
  @Inject
  public HgPackageReader(CacheManager cacheManager,
                         Provider<HttpClient> httpClientProvider)
  {
    cache = cacheManager.getCache(String.class, HgPackages.class, CACHENAME);
    this.httpClientProvider = httpClientProvider;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  public HgPackage getPackage(String id)
  {
    HgPackage pkg = null;

    for (HgPackage p : getPackages())
    {
      if (id.equals(p.getId()))
      {
        pkg = p;

        break;
      }
    }

    return pkg;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public HgPackages getPackages()
  {
    HgPackages packages = cache.get(HgPackages.class.getName());

    if (packages == null)
    {
      packages = getRemptePackages();
      filterPackage(packages);
      cache.put(HgPackages.class.getName(), packages);
    }

    return packages;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param packages
   */
  private void filterPackage(HgPackages packages)
  {
    List<HgPackage> pkgList = new ArrayList<HgPackage>();

    for (HgPackage pkg : packages)
    {
      boolean add = true;

      if (Util.isNotEmpty(pkg.getPlatform()))
      {
        PlatformType pt = PlatformType.createPlatformType(pkg.getPlatform());

        if (SystemUtil.getPlatform().getType() != pt)
        {
          if (logger.isDebugEnabled())
          {
            logger.debug("reject package {}, because of wrong platform {}",
                         pkg.getId(), pkg.getPlatform());
          }

          add = false;
        }
      }

      if (add && Util.isNotEmpty(pkg.getArch()))
      {
        if (!SystemUtil.getArch().equals(pkg.getArch()))
        {
          if (logger.isDebugEnabled())
          {
            logger.debug("reject package {}, because of wrong arch {}",
                         pkg.getId(), pkg.getArch());
          }

          add = false;
        }
      }

      if (add)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("added HgPackage {}", pkg.getId());
        }

        pkgList.add(pkg);
      }
    }

    packages.setPackages(pkgList);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private HgPackages getRemptePackages()
  {
    if (logger.isInfoEnabled())
    {
      logger.info("fetch HgPackages from {}", PACKAGEURL);
    }

    HgPackages packages = null;
    InputStream input = null;

    try
    {
      HttpRequest request = new HttpRequest(PACKAGEURL);

      request.setDecodeGZip(true);

      HttpResponse response = httpClientProvider.get().get(request);

      input = response.getContent();
      packages = JAXB.unmarshal(input, HgPackages.class);
    }
    catch (IOException ex)
    {
      logger.error("could not read HgPackages from ".concat(PACKAGEURL), ex);
    }
    finally
    {
      IOUtil.close(input);
    }

    if (packages == null)
    {
      packages = new HgPackages();
      packages.setPackages(new ArrayList<HgPackage>());
    }

    return packages;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache<String, HgPackages> cache;

  /** Field description */
  private Provider<HttpClient> httpClientProvider;
}
