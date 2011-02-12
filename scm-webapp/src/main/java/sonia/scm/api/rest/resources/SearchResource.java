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
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import sonia.scm.HandlerEvent;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.group.Group;
import sonia.scm.group.GroupListener;
import sonia.scm.group.GroupManager;
import sonia.scm.search.SearchHandler;
import sonia.scm.search.SearchResult;
import sonia.scm.search.SearchResults;
import sonia.scm.user.User;
import sonia.scm.user.UserListener;
import sonia.scm.user.UserManager;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("search")
public class SearchResource implements UserListener, GroupListener
{

  /** Field description */
  public static final String CACHE_GROUP = "sonia.cache.search.groups";

  /** Field description */
  public static final String CACHE_USER = "sonia.cache.search.users";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param securityContextProvider
   * @param userManager
   * @param groupManager
   * @param cacheManager
   */
  @Inject
  public SearchResource(Provider<WebSecurityContext> securityContextProvider,
                        UserManager userManager, GroupManager groupManager,
                        CacheManager cacheManager)
  {

    // create user searchhandler
    userManager.addListener(this);

    Cache<String, SearchResults> userCache =
      cacheManager.getCache(String.class, SearchResults.class, CACHE_USER);

    this.userSearchHandler = new SearchHandler<User>(securityContextProvider,
            userCache, userManager);

    // create group searchhandler
    groupManager.addListener(this);

    Cache<String, SearchResults> groupCache =
      cacheManager.getCache(String.class, SearchResults.class, CACHE_GROUP);

    this.groupSearchHandler = new SearchHandler<Group>(securityContextProvider,
            groupCache, groupManager);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   * @param event
   */
  @Override
  public void onEvent(User user, HandlerEvent event)
  {
    userSearchHandler.clearCache();
  }

  /**
   * Method description
   *
   *
   * @param group
   * @param event
   */
  @Override
  public void onEvent(Group group, HandlerEvent event)
  {
    groupSearchHandler.clearCache();
  }

  /**
   * Method description
   *
   *
   * @param queryString
   *
   * @return
   */
  @GET
  @Path("groups")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public SearchResults searchGroups(@QueryParam("query") String queryString)
  {
    return groupSearchHandler.search(queryString,
                                     new Function<Group, SearchResult>()
    {
      @Override
      public SearchResult apply(Group group)
      {
        String label = group.getName();
        String description = group.getDescription();

        if (description != null)
        {
          label = label.concat(" (").concat(description).concat(")");
        }

        return new SearchResult(group.getName(), label);
      }
    });
  }

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
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public SearchResults searchUsers(@QueryParam("query") String queryString)
  {
    return userSearchHandler.search(queryString,
                                    new Function<User, SearchResult>()
    {
      @Override
      public SearchResult apply(User user)
      {
        StringBuilder label = new StringBuilder(user.getName());

        label.append(" (").append(user.getDisplayName()).append(")");

        return new SearchResult(user.getName(), label.toString());
      }
    });
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SearchHandler<Group> groupSearchHandler;

  /** Field description */
  private SearchHandler<User> userSearchHandler;
}
