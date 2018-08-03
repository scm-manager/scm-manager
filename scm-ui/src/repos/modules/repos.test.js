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
  fetchReposByPage,
  FETCH_REPO,
  fetchRepo,
  FETCH_REPO_PENDING,
  FETCH_REPO_SUCCESS,
  FETCH_REPO_FAILURE,
  fetchRepoSuccess,
  getRepository,
  isFetchRepoPending,
  getFetchRepoFailure,
  CREATE_REPO_PENDING,
  CREATE_REPO_SUCCESS,
  createRepo,
  CREATE_REPO_FAILURE,
  isCreateRepoPending,
  CREATE_REPO,
  getCreateRepoFailure,
  isAbleToCreateRepos,
  DELETE_REPO,
  DELETE_REPO_SUCCESS,
  deleteRepo,
  DELETE_REPO_PENDING,
  DELETE_REPO_FAILURE,
  isDeleteRepoPending,
  getDeleteRepoFailure
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
  const REPOS_URL = "/scm/api/rest/v2/repositories";
  const SORT = "sortBy=namespaceAndName";
  const REPOS_URL_WITH_SORT = REPOS_URL + "?" + SORT;
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch repos", () => {
    fetchMock.getOnce(REPOS_URL_WITH_SORT, repositoryCollection);

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
    const url = REPOS_URL + "?page=42&" + SORT;
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
    fetchMock.getOnce(
      REPOS_URL + "?" + SORT + "&page=42",
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
    fetchMock.getOnce(REPOS_URL_WITH_SORT, {
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

  it("should successfully fetch repo slarti/fjords", () => {
    fetchMock.getOnce(REPOS_URL + "/slarti/fjords", slartiFjords);

    const expectedActions = [
      {
        type: FETCH_REPO_PENDING,
        payload: {
          namespace: "slarti",
          name: "fjords"
        },
        itemId: "slarti/fjords"
      },
      {
        type: FETCH_REPO_SUCCESS,
        payload: slartiFjords,
        itemId: "slarti/fjords"
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchRepo("slarti", "fjords")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_REPO_FAILURE, it the request for slarti/fjords fails", () => {
    fetchMock.getOnce(REPOS_URL + "/slarti/fjords", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchRepo("slarti", "fjords")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_REPO_PENDING);
      expect(actions[1].type).toEqual(FETCH_REPO_FAILURE);
      expect(actions[1].payload.namespace).toBe("slarti");
      expect(actions[1].payload.name).toBe("fjords");
      expect(actions[1].payload.error).toBeDefined();
      expect(actions[1].itemId).toBe("slarti/fjords");
    });
  });

  it("should successfully create repo slarti/fjords", () => {
    fetchMock.postOnce(REPOS_URL, {
      status: 201
    });

    const expectedActions = [
      {
        type: CREATE_REPO_PENDING
      },
      {
        type: CREATE_REPO_SUCCESS
      }
    ];

    const store = mockStore({});
    return store.dispatch(createRepo(slartiFjords)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should successfully create repo slarti/fjords and call the callback", () => {
    fetchMock.postOnce(REPOS_URL, {
      status: 201
    });

    let callMe = "not yet";

    const callback = () => {
      callMe = "yeah";
    };

    const store = mockStore({});
    return store.dispatch(createRepo(slartiFjords, callback)).then(() => {
      expect(callMe).toBe("yeah");
    });
  });

  it("should disapatch failure if server returns status code 500", () => {
    fetchMock.postOnce(REPOS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(createRepo(slartiFjords)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(CREATE_REPO_PENDING);
      expect(actions[1].type).toEqual(CREATE_REPO_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should successfully delete repo slarti/fjords", () => {
    fetchMock.delete(
      "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords",
      {
        status: 204
      }
    );

    const expectedActions = [
      {
        type: DELETE_REPO_PENDING,
        payload: slartiFjords,
        itemId: "slarti/fjords"
      },
      {
        type: DELETE_REPO_SUCCESS,
        payload: slartiFjords,
        itemId: "slarti/fjords"
      }
    ];

    const store = mockStore({});
    return store.dispatch(deleteRepo(slartiFjords)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should successfully delete repo slarti/fjords and call the callback", () => {
    fetchMock.delete(
      "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords",
      {
        status: 204
      }
    );

    let callMe = "not yet";

    const callback = () => {
      callMe = "yeah";
    };

    const store = mockStore({});
    return store.dispatch(deleteRepo(slartiFjords, callback)).then(() => {
      expect(callMe).toBe("yeah");
    });
  });

  it("should disapatch failure on delete, if server returns status code 500", () => {
    fetchMock.delete(
      "http://localhost:8081/scm/api/rest/v2/repositories/slarti/fjords",
      {
        status: 500
      }
    );

    const store = mockStore({});
    return store.dispatch(deleteRepo(slartiFjords)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(DELETE_REPO_PENDING);
      expect(actions[1].type).toEqual(DELETE_REPO_FAILURE);
      expect(actions[1].payload.repository).toBe(slartiFjords);
      expect(actions[1].payload.error).toBeDefined();
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

  it("should store the repo at byNames", () => {
    const newState = reducer({}, fetchRepoSuccess(slartiFjords));
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

  it("should return the repository collection", () => {
    const state = {
      repos: {
        byNames: {
          "slarti/fjords": slartiFjords
        }
      }
    };

    const repository = getRepository(state, "slarti", "fjords");
    expect(repository).toEqual(slartiFjords);
  });

  it("should return true, when fetch repo is pending", () => {
    const state = {
      pending: {
        [FETCH_REPO + "/slarti/fjords"]: true
      }
    };
    expect(isFetchRepoPending(state, "slarti", "fjords")).toEqual(true);
  });

  it("should return false, when fetch repo is not pending", () => {
    expect(isFetchRepoPending({}, "slarti", "fjords")).toEqual(false);
  });

  it("should return error when fetch repo did fail", () => {
    const state = {
      failure: {
        [FETCH_REPO + "/slarti/fjords"]: error
      }
    };
    expect(getFetchRepoFailure(state, "slarti", "fjords")).toEqual(error);
  });

  it("should return undefined when fetch repo did not fail", () => {
    expect(getFetchRepoFailure({}, "slarti", "fjords")).toBe(undefined);
  });

  // create

  it("should return true, when create repo is pending", () => {
    const state = {
      pending: {
        [CREATE_REPO]: true
      }
    };
    expect(isCreateRepoPending(state)).toEqual(true);
  });

  it("should return false, when create repo is not pending", () => {
    expect(isCreateRepoPending({})).toEqual(false);
  });

  it("should return error when create repo did fail", () => {
    const state = {
      failure: {
        [CREATE_REPO]: error
      }
    };
    expect(getCreateRepoFailure(state)).toEqual(error);
  });

  it("should return undefined when create repo did not fail", () => {
    expect(getCreateRepoFailure({})).toBe(undefined);
  });

  // delete

  it("should return true, when delete repo is pending", () => {
    const state = {
      pending: {
        [DELETE_REPO + "/slarti/fjords"]: true
      }
    };
    expect(isDeleteRepoPending(state, "slarti", "fjords")).toEqual(true);
  });

  it("should return false, when delete repo is not pending", () => {
    expect(isDeleteRepoPending({}, "slarti", "fjords")).toEqual(false);
  });

  it("should return error when delete repo did fail", () => {
    const state = {
      failure: {
        [DELETE_REPO + "/slarti/fjords"]: error
      }
    };
    expect(getDeleteRepoFailure(state, "slarti", "fjords")).toEqual(error);
  });

  it("should return undefined when delete repo did not fail", () => {
    expect(getDeleteRepoFailure({}, "slarti", "fjords")).toBe(undefined);
  });

  it("should return true if the list contains the create link", () => {
    const state = {
      repos: {
        list: repositoryCollection
      }
    };

    expect(isAbleToCreateRepos(state)).toBe(true);
  });

  it("should return false, if create link is unavailable", () => {
    const state = {
      repos: {
        list: {
          _links: {}
        }
      }
    };

    expect(isAbleToCreateRepos(state)).toBe(false);
  });
});
