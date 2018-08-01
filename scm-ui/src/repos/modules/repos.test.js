// @flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  FETCH_REPOS_PENDING,
  FETCH_REPOS_SUCCESS,
  fetchRepos,
  FETCH_REPOS_FAILURE,
  fetchReposSuccess,
  getRepositoryCollection,
  FETCH_REPOS,
  isFetchReposPending,
  getFetchReposFailure,
  fetchReposByLink,
  fetchReposByPage
} from "./repos";
import type { Repository, RepositoryCollection } from "../types/Repositories";

const hitchhikerPuzzle42: Repository = {
  contact: "fourtytwo@hitchhiker.com",
  creationDate: "2018-07-31T08:58:45.961Z",
  description: "the answer to life the universe and everything",
  namespace: "hitchhiker",
  name: "puzzle42",
  type: "svn",
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42"
    },
    delete: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42"
    },
    update: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42"
    },
    permissions: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/permissions/"
    },
    tags: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/tags/"
    },
    branches: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/branches/"
    },
    changesets: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/changesets/"
    },
    sources: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/sources/"
    }
  }
};

const hitchhikerRestatend: Repository = {
  contact: "restatend@hitchhiker.com",
  creationDate: "2018-07-31T08:58:32.803Z",
  description: "restaurant at the end of the universe",
  namespace: "hitchhiker",
  name: "restatend",
  archived: false,
  type: "git",
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/restatend"
    },
    delete: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/restatend"
    },
    update: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/restatend"
    },
    permissions: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/restatend/permissions/"
    },
    tags: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/restatend/tags/"
    },
    branches: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/restatend/branches/"
    },
    changesets: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/restatend/changesets/"
    },
    sources: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/restatend/sources/"
    }
  }
};

const slartiFjords: Repository = {
  contact: "slartibartfast@hitchhiker.com",
  description: "My award-winning fjords from the Norwegian coast",
  namespace: "slarti",
  name: "fjords",
  type: "hg",
  creationDate: "2018-07-31T08:59:05.653Z",
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords"
    },
    delete: {
      href: "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords"
    },
    update: {
      href: "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords"
    },
    permissions: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords/permissions/"
    },
    tags: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords/tags/"
    },
    branches: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords/branches/"
    },
    changesets: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords/changesets/"
    },
    sources: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords/sources/"
    }
  }
};

const repositoryCollection: RepositoryCollection = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/?page=0&pageSize=10"
    },
    first: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/?page=0&pageSize=10"
    },
    last: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:8081/scm/api/rest/v2/repositories/"
    }
  },
  _embedded: {
    repositories: [hitchhikerPuzzle42, hitchhikerRestatend, slartiFjords]
  }
};

const repositoryCollectionWithNames: RepositoryCollection = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/?page=0&pageSize=10"
    },
    first: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/?page=0&pageSize=10"
    },
    last: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:8081/scm/api/rest/v2/repositories/"
    }
  },
  _embedded: {
    repositories: [
      "hitchhiker/puzzle42",
      "hitchhiker/restatend",
      "slarti/fjords"
    ]
  }
};

describe("repos fetch", () => {
  const REPOS_URL = "/scm/api/rest/v2/repositories?sortBy=namespaceAndName";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch repos", () => {
    const url = REPOS_URL + "&page=42";
    fetchMock.getOnce(url, repositoryCollection);

    const expectedActions = [
      { type: FETCH_REPOS_PENDING },
      {
        type: FETCH_REPOS_SUCCESS,
        payload: repositoryCollection
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchRepos()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should successfully fetch page 42", () => {
    const url = REPOS_URL + "&page=42";
    fetchMock.getOnce(url, repositoryCollection);

    const expectedActions = [
      { type: FETCH_REPOS_PENDING },
      {
        type: FETCH_REPOS_SUCCESS,
        payload: repositoryCollection
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchReposByPage(43)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should successfully fetch repos from link", () => {
    fetchMock.getOnce(REPOS_URL, repositoryCollection);

    const expectedActions = [
      { type: FETCH_REPOS_PENDING },
      {
        type: FETCH_REPOS_SUCCESS,
        payload: repositoryCollection
      }
    ];

    const store = mockStore({});
    return store
      .dispatch(
        fetchReposByLink("/repositories?sortBy=namespaceAndName&page=42")
      )
      .then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
  });

  it("should append sortby parameter and successfully fetch repos from link", () => {
    fetchMock.getOnce(
      "/scm/api/rest/v2/repositories?one=1&sortBy=namespaceAndName",
      repositoryCollection
    );

    const expectedActions = [
      { type: FETCH_REPOS_PENDING },
      {
        type: FETCH_REPOS_SUCCESS,
        payload: repositoryCollection
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchReposByLink("/repositories?one=1")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_REPOS_FAILURE, it the request fails", () => {
    fetchMock.getOnce(REPOS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchRepos()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_REPOS_PENDING);
      expect(actions[1].type).toEqual(FETCH_REPOS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("repos reducer", () => {
  it("should return empty object, if state and action is undefined", () => {
    expect(reducer()).toEqual({});
  });

  it("should return the same state, if the action is undefined", () => {
    const state = { x: true };
    expect(reducer(state)).toBe(state);
  });

  it("should return the same state, if the action is unknown to the reducer", () => {
    const state = { x: true };
    expect(reducer(state, { type: "EL_SPECIALE" })).toBe(state);
  });

  it("should store the repositories by it's namespace and name on FETCH_REPOS_SUCCESS", () => {
    const newState = reducer({}, fetchReposSuccess(repositoryCollection));
    expect(newState.list.page).toBe(0);
    expect(newState.list.pageTotal).toBe(1);
    expect(newState.list._embedded.repositories).toEqual([
      "hitchhiker/puzzle42",
      "hitchhiker/restatend",
      "slarti/fjords"
    ]);

    expect(newState.byNames["hitchhiker/puzzle42"]).toBe(hitchhikerPuzzle42);
    expect(newState.byNames["hitchhiker/restatend"]).toBe(hitchhikerRestatend);
    expect(newState.byNames["slarti/fjords"]).toBe(slartiFjords);
  });
});

describe("repos selectors", () => {
  const error = new Error("something goes wrong");

  it("should return the repositories collection", () => {
    const state = {
      repos: {
        list: repositoryCollectionWithNames,
        byNames: {
          "hitchhiker/puzzle42": hitchhikerPuzzle42,
          "hitchhiker/restatend": hitchhikerRestatend,
          "slarti/fjords": slartiFjords
        }
      }
    };

    const collection = getRepositoryCollection(state);
    expect(collection).toEqual(repositoryCollection);
  });

  it("should return true, when fetch repos is pending", () => {
    const state = {
      pending: {
        [FETCH_REPOS]: true
      }
    };
    expect(isFetchReposPending(state)).toEqual(true);
  });

  it("should return false, when fetch repos is not pending", () => {
    expect(isFetchReposPending({})).toEqual(false);
  });

  it("should return error when fetch repos did fail", () => {
    const state = {
      failure: {
        [FETCH_REPOS]: error
      }
    };
    expect(getFetchReposFailure(state)).toEqual(error);
  });

  it("should return undefined when fetch repos did not fail", () => {
    expect(getFetchReposFailure({})).toBe(undefined);
  });
});
