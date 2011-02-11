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



package sonia.scm.api.rest.resources;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import sonia.scm.api.rest.SearchResult;
import sonia.scm.api.rest.SearchResults;
import sonia.scm.search.SearchRequest;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.util.SecurityUtil;
import sonia.scm.util.Util;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("search")
public class SearchResource
{

  /**
   * Constructs ...
   *
   *
   * @param securityContextProvider
   * @param userManager
   */
  @Inject
  public SearchResource(Provider<WebSecurityContext> securityContextProvider,
                        UserManager userManager)
  {
    this.securityContextProvider = securityContextProvider;
    this.userManager = userManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param queryString
   *
   * @return
   */
  @GET
  @Path("users")
  public SearchResults searchUsers(@QueryParam("query") String queryString)
  {
    SecurityUtil.assertIsNotAnonymous(securityContextProvider);

    if (Util.isEmpty(queryString))
    {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    SearchRequest request = new SearchRequest(queryString, true);

    request.setMaxResults(5);

    Collection<User> users = userManager.search(request);
    SearchResults result = new SearchResults();

    if (Util.isNotEmpty(users))
    {
      Collection<SearchResult> resultCollection = Collections2.transform(users,
                                                    new Function<User,
                                                      SearchResult>()
      {
        @Override
        public SearchResult apply(User user)
        {
          return new SearchResult(user.getName(), user.getDisplayName());
        }
      });

      result.setSuccess(true);
      result.setResults(resultCollection);
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Provider<WebSecurityContext> securityContextProvider;

  /** Field description */
  private UserManager userManager;
}
