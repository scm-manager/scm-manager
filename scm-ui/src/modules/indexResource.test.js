import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  FETCH_INDEXRESOURCES_PENDING,
  FETCH_INDEXRESOURCES_SUCCESS,
  FETCH_INDEXRESOURCES_FAILURE,
  fetchIndexResources
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
    }
  }
};

describe("fetch index resource", () => {
  const index_url = "/api/v2/";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch index resources when unauthenticated", () => {
    fetchMock.getOnce(index_url, indexResourcesUnauthenticated);

    const expectedActions = [
      { type: FETCH_INDEXRESOURCES_PENDING },
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
    fetchMock.getOnce(index_url, indexResourcesAuthenticated);

    const expectedActions = [
      { type: FETCH_INDEXRESOURCES_PENDING },
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
    fetchMock.getOnce(index_url, {
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
});
