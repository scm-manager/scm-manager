/**
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


package sonia.scm.installer;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.PlatformType;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.util.SystemUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

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
    "http://download.scm-manager.org/pkg/mercurial/packages.xml";

  /** the logger for HgPackageReader */
  private static final Logger logger =
    LoggerFactory.getLogger(HgPackageReader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param cacheManager
   * @param httpClient
   */
  @Inject
  public HgPackageReader(CacheManager cacheManager, AdvancedHttpClient httpClient)
  {
    this.cache = cacheManager.getCache(CACHENAME);
    this.httpClient = httpClient;
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
    List<HgPackage> pkgList = new ArrayList<>();

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

    try
    {
      //J-
      packages = httpClient.get(PACKAGEURL)
                           .request()
                           .contentFromXml(HgPackages.class);
      //J+
    }
    catch (IOException ex)
    {
      logger.error("could not read HgPackages from ".concat(PACKAGEURL), ex);
    }

    if (packages == null)
    {
      packages = new HgPackages();
      packages.setPackages(new ArrayList<>());
    }

    return packages;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Cache<String, HgPackages> cache;

  /** Field description */
  private final AdvancedHttpClient httpClient;
}
