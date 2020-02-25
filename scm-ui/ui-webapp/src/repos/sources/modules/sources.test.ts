import { File, Repository } from "@scm-manager/ui-types";
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  default as reducer,
  FETCH_SOURCES,
  FETCH_SOURCES_FAILURE,
  FETCH_SOURCES_PENDING,
  FETCH_SOURCES_SUCCESS,
  fetchSources,
  fetchSourcesSuccess,
  getFetchSourcesFailure,
  getSources,
  isDirectory,
  isFetchSourcesPending
} from "./sources";

const sourcesUrl = "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources/";

const repository: Repository = {
  name: "core",
  namespace: "scm",
  type: "git",
  _links: {
    sources: {
      href: sourcesUrl
    }
  }
};

const collection = {
  name: "src",
  path: "src",
  directory: true,
  description: "foo",
  length: 176,
  revision: "76aae4bb4ceacf0e88938eb5b6832738b7d537b4",
  subRepository: undefined,
  truncated: true,
  partialResult: false,
  computationAborted: false,
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/"
    }
  },
  _embedded: {
    children: [
      {
        name: "src",
        path: "src",
        directory: true,
        length: 176,
        revision: "76aae4bb4ceacf0e88938eb5b6832738b7d537b4",
        subRepository: undefined,
        _links: {
          self: {
            href:
              "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/src"
          }
        },
        _embedded: {
          children: []
        }
      },
      {
        name: "package.json",
        path: "package.json",
        directory: false,
        description: "bump version",
        length: 780,
        revision: "76aae4bb4ceacf0e88938eb5b6832738b7d537b4",
        commitDate: "2017-07-31T11:17:19Z",
        subRepository: undefined,
        _links: {
          self: {
            href:
              "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/content/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/package.json"
          },
          history: {
            href:
              "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources/history/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/package.json"
          }
        },
        _embedded: {
          children: []
        }
      }
    ]
  }
};

const noDirectory: File = {
  name: "src",
  path: "src",
  directory: true,
  length: 176,
  revision: "abc",
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/src"
    }
  },
  _embedded: {
    children: []
  }
};

describe("sources fetch", () => {
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should fetch the sources of the repository", () => {
    fetchMock.getOnce(sourcesUrl + "?offset=0", collection);

    const expectedActions = [
      {
        type: FETCH_SOURCES_PENDING,
        itemId: "scm/core/_//",
        payload: {
          hunk: 0,
          updatePending: false,
          pending: true,
          sources: {}
        }
      },
      {
        type: FETCH_SOURCES_SUCCESS,
        itemId: "scm/core/_//",
        payload: {
          hunk: 0,
          updatePending: false,
          pending: false,
          sources: collection
        }
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchSources(repository, "", "")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fetch the sources of the repository with the given revision and path", () => {
    fetchMock.getOnce(sourcesUrl + "abc/src?offset=0", collection);

    const expectedActions = [
      {
        type: FETCH_SOURCES_PENDING,
        itemId: "scm/core/abc/src/",
        payload: {
          hunk: 0,
          updatePending: false,
          pending: true,
          sources: {}
        }
      },
      {
        type: FETCH_SOURCES_SUCCESS,
        itemId: "scm/core/abc/src/",
        payload: {
          hunk: 0,
          updatePending: false,
          pending: false,
          sources: collection
        }
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchSources(repository, "abc", "src")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_SOURCES_FAILURE on server error", () => {
    fetchMock.getOnce(sourcesUrl + "?offset=0", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchSources(repository, "", "")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toBe(FETCH_SOURCES_PENDING);
      expect(actions[1].type).toBe(FETCH_SOURCES_FAILURE);
      expect(actions[1].itemId).toBe("scm/core/_//");
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("reducer tests", () => {
  it("should return unmodified state on unknown action", () => {
    const state = {};
    expect(reducer(state)).toBe(state);
  });

  it("should store the collection, without revision and path", () => {
    const expectedState = {
      "scm/core/_//0": { pending: false, updatePending: false, sources: collection },
      "scm/core/_//hunkCount": 1
    };
    expect(reducer({}, fetchSourcesSuccess(repository, "", "", 0, collection))).toEqual(expectedState);
  });

  it("should store the collection, with revision and path", () => {
    const expectedState = {
      "scm/core/abc/src/main/0": { pending: false, updatePending: false, sources: collection },
      "scm/core/abc/src/main/hunkCount": 1
    };
    expect(reducer({}, fetchSourcesSuccess(repository, "abc", "src/main", 0, collection))).toEqual(expectedState);
  });
});

describe("selector tests", () => {
  it("should return false if it is no directory", () => {
    const state = {
      sources: {
        "scm/core/abc/src/main/package.json/0": {
          sources: { noDirectory }
        }
      }
    };
    expect(isDirectory(state, repository, "abc", "src/main/package.json")).toBeFalsy();
  });

  it("should return true if it is directory", () => {
    const state = {
      sources: {
        "scm/core/abc/src/0": noDirectory
      }
    };
    expect(isDirectory(state, repository, "abc", "src")).toBe(true);
  });

  it("should return null", () => {
    expect(getSources({}, repository, "", "")).toBeFalsy();
  });

  it("should return the source collection without revision and path", () => {
    const state = {
      sources: {
        "scm/core/_//0": {
          sources: collection
        }
      }
    };
    expect(getSources(state, repository, "", "")).toBe(collection);
  });

  it("should return the source collection with revision and path", () => {
    const state = {
      sources: {
        "scm/core/abc/src/main/0": {
          sources: collection
        }
      }
    };
    expect(getSources(state, repository, "abc", "src/main")).toBe(collection);
  });

  it("should return true, when fetch sources is pending", () => {
    const state = {
      sources: {
        "scm/core/_//0": {
          pending: true,
          sources: {}
        }
      }
    };
    expect(isFetchSourcesPending(state, repository, "", "", 0)).toEqual(true);
  });

  it("should return false, when fetch sources is not pending", () => {
    const state = {
      sources: {
        "scm/core/_//0": {
          pending: false,
          sources: {}
        }
      }
    };
    expect(isFetchSourcesPending(state, repository, "", "", 0)).toEqual(false);
  });

  const error = new Error("incredible error from hell");

  it("should return error when fetch sources did fail", () => {
    const state = {
      failure: {
        [FETCH_SOURCES + "/scm/core/_//"]: error
      }
    };
    expect(getFetchSourcesFailure(state, repository, "", "", 0)).toEqual(error);
  });

  it("should return undefined when fetch sources did not fail", () => {
    expect(getFetchSourcesFailure({}, repository, "", "", 0)).toBe(undefined);
  });
});
