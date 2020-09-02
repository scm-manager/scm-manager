/*
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

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  FETCH_INDEXRESOURCES,
  FETCH_INDEXRESOURCES_FAILURE,
  FETCH_INDEXRESOURCES_PENDING,
  FETCH_INDEXRESOURCES_SUCCESS,
  fetchIndexResources,
  fetchIndexResourcesSuccess,
  getConfigLink,
  getFetchIndexResourcesFailure,
  getGitConfigLink,
  getGroupAutoCompleteLink,
  getGroupsLink,
  getHgConfigLink,
  getLinkCollection,
  getLinks,
  getLoginLink,
  getLogoutLink,
  getMeLink,
  getRepositoriesLink,
  getSvnConfigLink,
  getUiPluginsLink,
  getUserAutoCompleteLink,
  getUsersLink,
  isFetchIndexResourcesPending
} from "./indexResource";

const indexResourcesUnauthenticated = {
  version: "2.0.0-SNAPSHOT",
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/"
    },
    uiPlugins: {
      href: "http://localhost:8081/scm/api/v2/ui/plugins"
    },
    login: {
      href: "http://localhost:8081/scm/api/v2/auth/access_token"
    }
  }
};

const indexResourcesAuthenticated = {
  version: "2.0.0-SNAPSHOT",
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/"
    },
    uiPlugins: {
      href: "http://localhost:8081/scm/api/v2/ui/plugins"
    },
    me: {
      href: "http://localhost:8081/scm/api/v2/me/"
    },
    logout: {
      href: "http://localhost:8081/scm/api/v2/auth/access_token"
    },
    users: {
      href: "http://localhost:8081/scm/api/v2/users/"
    },
    groups: {
      href: "http://localhost:8081/scm/api/v2/groups/"
    },
    config: {
      href: "http://localhost:8081/scm/api/v2/config"
    },
    repositories: {
      href: "http://localhost:8081/scm/api/v2/repositories/"
    },
    hgConfig: {
      href: "http://localhost:8081/scm/api/v2/config/hg"
    },
    gitConfig: {
      href: "http://localhost:8081/scm/api/v2/config/git"
    },
    svnConfig: {
      href: "http://localhost:8081/scm/api/v2/config/svn"
    },
    autocomplete: [
      {
        href: "http://localhost:8081/scm/api/v2/autocomplete/users",
        name: "users"
      },
      {
        href: "http://localhost:8081/scm/api/v2/autocomplete/groups",
        name: "groups"
      }
    ]
  }
};

describe("index resource", () => {
  describe("fetch index resource", () => {
    const indexUrl = "/api/v2/";
    const mockStore = configureMockStore([thunk]);

    afterEach(() => {
      fetchMock.reset();
      fetchMock.restore();
    });

    it("should successfully fetch index resources when unauthenticated", () => {
      fetchMock.getOnce(indexUrl, indexResourcesUnauthenticated);

      const expectedActions = [
        {
          type: FETCH_INDEXRESOURCES_PENDING
        },
        {
          type: FETCH_INDEXRESOURCES_SUCCESS,
          payload: indexResourcesUnauthenticated
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchIndexResources()).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should successfully fetch index resources when authenticated", () => {
      fetchMock.getOnce(indexUrl, indexResourcesAuthenticated);

      const expectedActions = [
        {
          type: FETCH_INDEXRESOURCES_PENDING
        },
        {
          type: FETCH_INDEXRESOURCES_SUCCESS,
          payload: indexResourcesAuthenticated
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchIndexResources()).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should dispatch FETCH_INDEX_RESOURCES_FAILURE if request fails", () => {
      fetchMock.getOnce(indexUrl, {
        status: 500
      });

      const store = mockStore({});
      return store.dispatch(fetchIndexResources()).then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(FETCH_INDEXRESOURCES_PENDING);
        expect(actions[1].type).toEqual(FETCH_INDEXRESOURCES_FAILURE);
        expect(actions[1].payload).toBeDefined();
      });
    });

    it("should retry to fetch index resource on unauthorized error", () => {
      fetchMock.getOnce(indexUrl, { status: 401 }).getOnce(indexUrl, indexResourcesAuthenticated, {
        overwriteRoutes: false
      });

      const expectedActions = [
        {
          type: FETCH_INDEXRESOURCES_PENDING
        },
        {
          type: FETCH_INDEXRESOURCES_SUCCESS,
          payload: indexResourcesAuthenticated
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchIndexResources()).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should only retry to fetch index resource on unauthorized error once", () => {
      fetchMock
        .getOnce(indexUrl, { status: 401 })
        .getOnce(
          indexUrl,
          { status: 401 },
          {
            overwriteRoutes: false
          }
        )
        .getOnce(indexUrl, indexResourcesAuthenticated, {
          overwriteRoutes: false
        });

      const store = mockStore({});
      return store.dispatch(fetchIndexResources()).then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(FETCH_INDEXRESOURCES_PENDING);
        expect(actions[1].type).toEqual(FETCH_INDEXRESOURCES_FAILURE);
        expect(actions[1].payload).toBeDefined();
      });
    });
  });

  describe("index resources reducer", () => {
    it("should return empty object, if state and action is undefined", () => {
      expect(reducer()).toEqual({});
    });

    it("should return the same state, if the action is undefined", () => {
      const state = {
        x: true
      };
      expect(reducer(state)).toBe(state);
    });

    it("should return the same state, if the action is unknown to the reducer", () => {
      const state = {
        x: true
      };
      expect(
        reducer(state, {
          type: "EL_SPECIALE"
        })
      ).toBe(state);
    });

    it("should store the index resources on FETCH_INDEXRESOURCES_SUCCESS", () => {
      const newState = reducer({}, fetchIndexResourcesSuccess(indexResourcesAuthenticated));
      expect(newState.links).toBe(indexResourcesAuthenticated._links);
    });
  });

  describe("index resources selectors", () => {
    const error = new Error("something goes wrong");

    it("should return true, when fetch index resources is pending", () => {
      const state = {
        pending: {
          [FETCH_INDEXRESOURCES]: true
        }
      };
      expect(isFetchIndexResourcesPending(state)).toEqual(true);
    });

    it("should return false, when fetch index resources is not pending", () => {
      expect(isFetchIndexResourcesPending({})).toEqual(false);
    });

    it("should return error when fetch index resources did fail", () => {
      const state = {
        failure: {
          [FETCH_INDEXRESOURCES]: error
        }
      };
      expect(getFetchIndexResourcesFailure(state)).toEqual(error);
    });

    it("should return undefined when fetch index resources did not fail", () => {
      expect(getFetchIndexResourcesFailure({})).toBe(undefined);
    });

    it("should return all links", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getLinks(state)).toBe(indexResourcesAuthenticated._links);
    });

    // ui plugins link
    it("should return ui plugins link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getUiPluginsLink(state)).toBe("http://localhost:8081/scm/api/v2/ui/plugins");
    });

    it("should return ui plugins links when unauthenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getUiPluginsLink(state)).toBe("http://localhost:8081/scm/api/v2/ui/plugins");
    });

    // me link
    it("should return me link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getMeLink(state)).toBe("http://localhost:8081/scm/api/v2/me/");
    });

    it("should return undefined for me link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getMeLink(state)).toBe(undefined);
    });

    // logout link
    it("should return logout link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getLogoutLink(state)).toBe("http://localhost:8081/scm/api/v2/auth/access_token");
    });

    it("should return undefined for logout link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getLogoutLink(state)).toBe(undefined);
    });

    // login link
    it("should return login link when unauthenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getLoginLink(state)).toBe("http://localhost:8081/scm/api/v2/auth/access_token");
    });

    it("should return undefined for login link when authenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getLoginLink(state)).toBe(undefined);
    });

    // users link
    it("should return users link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getUsersLink(state)).toBe("http://localhost:8081/scm/api/v2/users/");
    });

    it("should return undefined for users link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getUsersLink(state)).toBe(undefined);
    });

    // groups link
    it("should return groups link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getGroupsLink(state)).toBe("http://localhost:8081/scm/api/v2/groups/");
    });

    it("should return undefined for groups link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getGroupsLink(state)).toBe(undefined);
    });

    // config link
    it("should return config link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getConfigLink(state)).toBe("http://localhost:8081/scm/api/v2/config");
    });

    it("should return undefined for config link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getConfigLink(state)).toBe(undefined);
    });

    // repositories link
    it("should return repositories link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getRepositoriesLink(state)).toBe("http://localhost:8081/scm/api/v2/repositories/");
    });

    it("should return config for repositories link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getRepositoriesLink(state)).toBe(undefined);
    });

    // hgConfig link
    it("should return hgConfig link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getHgConfigLink(state)).toBe("http://localhost:8081/scm/api/v2/config/hg");
    });

    it("should return config for hgConfig link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getHgConfigLink(state)).toBe(undefined);
    });

    // gitConfig link
    it("should return gitConfig link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getGitConfigLink(state)).toBe("http://localhost:8081/scm/api/v2/config/git");
    });

    it("should return config for gitConfig link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getGitConfigLink(state)).toBe(undefined);
    });

    // svnConfig link
    it("should return svnConfig link when authenticated and has permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getSvnConfigLink(state)).toBe("http://localhost:8081/scm/api/v2/config/svn");
    });

    it("should return config for svnConfig link when unauthenticated or has not permission to see it", () => {
      const state = {
        indexResources: {
          links: indexResourcesUnauthenticated._links
        }
      };
      expect(getSvnConfigLink(state)).toBe(undefined);
    });

    // Autocomplete links
    it("should return link collection", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getLinkCollection(state, "autocomplete")).toEqual(indexResourcesAuthenticated._links.autocomplete);
    });

    it("should return user autocomplete link", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getUserAutoCompleteLink(state)).toEqual("http://localhost:8081/scm/api/v2/autocomplete/users");
    });

    it("should return group autocomplete link", () => {
      const state = {
        indexResources: {
          links: indexResourcesAuthenticated._links
        }
      };
      expect(getGroupAutoCompleteLink(state)).toEqual("http://localhost:8081/scm/api/v2/autocomplete/groups");
    });
  });
});
