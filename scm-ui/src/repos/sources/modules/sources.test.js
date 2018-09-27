// @flow

import type { Repository } from "@scm-manager/ui-types";
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  FETCH_SOURCES,
  FETCH_SOURCES_FAILURE,
  FETCH_SOURCES_PENDING,
  FETCH_SOURCES_SUCCESS,
  fetchSources,
  getFetchSourcesFailure,
  isFetchSourcesPending,
  default as reducer,
  getSources
} from "./sources";

const sourcesUrl =
  "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources";

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
  revision: "76aae4bb4ceacf0e88938eb5b6832738b7d537b4",
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/"
    }
  },
  _embedded: {
    files: [
      {
        name: "src",
        path: "src",
        directory: true,
        description: null,
        length: 176,
        lastModified: null,
        subRepository: null,
        _links: {
          self: {
            href:
              "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/src"
          }
        }
      },
      {
        name: "package.json",
        path: "package.json",
        directory: false,
        description: "bump version",
        length: 780,
        lastModified: "2017-07-31T11:17:19Z",
        subRepository: null,
        _links: {
          self: {
            href:
              "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/content/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/package.json"
          },
          history: {
            href:
              "http://localhost:8081/scm/rest/api/v2/repositories/scm/core/sources/history/76aae4bb4ceacf0e88938eb5b6832738b7d537b4/package.json"
          }
        }
      }
    ]
  }
};

describe("sources fetch", () => {
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should fetch the sources of the repository", () => {
    fetchMock.getOnce(sourcesUrl, collection);

    const expectedActions = [
      { type: FETCH_SOURCES_PENDING, itemId: "scm/core" },
      {
        type: FETCH_SOURCES_SUCCESS,
        itemId: "scm/core",
        payload: collection
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchSources(repository)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_SOURCES_FAILURE on server error", () => {
    fetchMock.getOnce(sourcesUrl, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchSources(repository)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toBe(FETCH_SOURCES_PENDING);
      expect(actions[1].type).toBe(FETCH_SOURCES_FAILURE);
      expect(actions[1].itemId).toBe("scm/core");
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("reducer tests", () => {
  it("should return unmodified state on unknown action", () => {
    const state = {};
    expect(reducer(state)).toBe(state);
  });

  it("should store the collection", () => {
    const expectedState = {
      "scm/core": repository
    };
    expect(reducer({}, fetchSources(repository))).toEqual(expectedState);
  });
});

describe("selector tests", () => {
  it("should return null", () => {
    expect(getSources({}, repository)).toBeFalsy();
  });

  it("should return the source collection", () => {
    const state = {
      sources: {
        "scm/core": collection
      }
    };
    expect(getSources(state, repository)).toBe(collection);
  });

  it("should return true, when fetch sources is pending", () => {
    const state = {
      pending: {
        [FETCH_SOURCES + "/scm/core"]: true
      }
    };
    expect(isFetchSourcesPending(state, repository)).toEqual(true);
  });

  it("should return false, when fetch sources is not pending", () => {
    expect(isFetchSourcesPending({}, repository)).toEqual(false);
  });

  const error = new Error("incredible error from hell");

  it("should return error when fetch sources did fail", () => {
    const state = {
      failure: {
        [FETCH_SOURCES + "/scm/core"]: error
      }
    };
    expect(getFetchSourcesFailure(state, repository)).toEqual(error);
  });

  it("should return undefined when fetch sources did not fail", () => {
    expect(getFetchSourcesFailure({}, repository)).toBe(undefined);
  });
});
