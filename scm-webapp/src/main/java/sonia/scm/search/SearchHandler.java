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



package sonia.scm.search;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cache.Cache;
import sonia.scm.util.SecurityUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public class SearchHandler<T>
{

  /** the logger for SearchHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(SearchHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param securityContextProvider
   * @param cache
   * @param searchable
   */
  public SearchHandler(Provider<WebSecurityContext> securityContextProvider,
                       Cache<String, SearchResults> cache,
                       Searchable<T> searchable)
  {
    this.securityContextProvider = securityContextProvider;
    this.cache = cache;
    this.searchable = searchable;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  public void clearCache()
  {
    this.cache.clear();
  }

  /**
   * Method description
   *
   *
   * @param queryString
   * @param function
   *
   * @return
   */
  public SearchResults search(String queryString,
                              Function<T, SearchResult> function)
  {
    SecurityUtil.assertIsNotAnonymous(securityContextProvider);

    if (Util.isEmpty(queryString))
    {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    SearchResults result = cache.get(queryString);

    if (result == null)
    {
      SearchRequest request = new SearchRequest(queryString, ignoreCase);

      request.setMaxResults(maxResults);

      Collection<T> users = searchable.search(request);

      result = new SearchResults();

      if (Util.isNotEmpty(users))
      {
        Collection<SearchResult> resultCollection =
          Collections2.transform(users, function);

        result.setSuccess(true);
        result.setResults(resultCollection);
        cache.put(queryString, result);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("return searchresults for {} from cache", queryString);
    }

    return result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public int getMaxResults()
  {
    return maxResults;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isIgnoreCase()
  {
    return ignoreCase;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ignoreCase
   */
  public void setIgnoreCase(boolean ignoreCase)
  {
    this.ignoreCase = ignoreCase;
  }

  /**
   * Method description
   *
   *
   * @param maxResults
   */
  public void setMaxResults(int maxResults)
  {
    this.maxResults = maxResults;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected Cache<String, SearchResults> cache;

  /** Field description */
  protected Searchable<T> searchable;

  /** Field description */
  protected Provider<WebSecurityContext> securityContextProvider;

  /** Field description */
  private int maxResults = 5;

  /** Field description */
  private boolean ignoreCase = true;
}
