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

import com.github.legman.Subscribe;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;

import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.group.Group;
import sonia.scm.group.GroupEvent;
import sonia.scm.group.GroupManager;
import sonia.scm.search.SearchHandler;
import sonia.scm.search.SearchResult;
import sonia.scm.search.SearchResults;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserManager;

//~--- JDK imports ------------------------------------------------------------

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * RESTful Web Service Resource to search users and groups. This endpoint can be used to implement typeahead input 
 * fields for permissions.
 * 
 * @author Sebastian Sdorra
 */
@Singleton
@Path("search")
public class SearchResource
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
   * @param userManager
   * @param groupManager
   * @param cacheManager
   */
  @Inject
  public SearchResource(UserManager userManager, GroupManager groupManager,
    CacheManager cacheManager)
  {

    // create user searchhandler
    Cache<String, SearchResults> userCache = cacheManager.getCache(CACHE_USER);

    this.userSearchHandler = new SearchHandler<>(userCache, userManager);

    // create group searchhandler
    Cache<String, SearchResults> groupCache =
      cacheManager.getCache(CACHE_GROUP);

    this.groupSearchHandler = new SearchHandler<>(groupCache,
                                                  groupManager);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param event
   */
  @Subscribe
  public void onEvent(UserEvent event)
  {
    if (event.getEventType().isPost())
    {
      userSearchHandler.clearCache();
    }
  }

  /**
   * Method description
   *
   *
   * @param event
   */
  @Subscribe
  public void onEvent(GroupEvent event)
  {
    if (event.getEventType().isPost())
    {
      groupSearchHandler.clearCache();
    }
  }

  /**
   * Returns a list of groups found by the given search string.
   *
   * @param queryString the search string
   *
   * @return
   */
  @GET
  @Path("groups")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public SearchResults searchGroups(@QueryParam("query") String queryString)
  {
    final Function<Group, SearchResult> groupSearchResultFunction = group -> {
      String label = group.getName();
      String description = group.getDescription();

      if (description != null) {
        label = label.concat(" (").concat(description).concat(")");
      }

      return new SearchResult(group.getName(), label);
    };
    return groupSearchHandler.search(queryString,
                                     groupSearchResultFunction);
  }

  /**
   * Returns a list of users found by the given search string.
   *
   * @param queryString the search string
   *
   * @return
   */
  @GET
  @Path("users")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public SearchResults searchUsers(@QueryParam("query") String queryString)
  {
    final Function<User, SearchResult> userSearchResultFunction = user -> {
      StringBuilder label = new StringBuilder(user.getName());

      label.append(" (").append(user.getDisplayName()).append(")");

      return new SearchResult(user.getName(), label.toString());
    };
    return userSearchHandler.search(queryString,
                                    userSearchResultFunction);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final SearchHandler<Group> groupSearchHandler;

  /** Field description */
  private final SearchHandler<User> userSearchHandler;
}
