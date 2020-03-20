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
///
/// MIT License
///
/// Copyright (c) 2020-present Cloudogu GmbH and Contributors
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
///

import fetchMock from "fetch-mock";
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import reducer, {
  FETCH_REPOSITORY_TYPES,
  FETCH_REPOSITORY_TYPES_FAILURE,
  FETCH_REPOSITORY_TYPES_PENDING,
  FETCH_REPOSITORY_TYPES_SUCCESS,
  fetchRepositoryTypesIfNeeded,
  fetchRepositoryTypesSuccess,
  getFetchRepositoryTypesFailure,
  getRepositoryTypes,
  isFetchRepositoryTypesPending,
  shouldFetchRepositoryTypes
} from "./repositoryTypes";

const git = {
  name: "git",
  displayName: "Git",
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/repositoryTypes/git"
    }
  }
};

const hg = {
  name: "hg",
  displayName: "Mercurial",
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/repositoryTypes/hg"
    }
  }
};

const svn = {
  name: "svn",
  displayName: "Subversion",
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/repositoryTypes/svn"
    }
  }
};

const collection = {
  _embedded: {
    repositoryTypes: [git, hg, svn]
  },
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/repositoryTypes"
    }
  }
};

describe("repository types caching", () => {
  it("should fetch repository types, on empty state", () => {
    expect(shouldFetchRepositoryTypes({})).toBe(true);
  });

  it("should fetch repository types, if the state contains an empty array", () => {
    const state = {
      repositoryTypes: []
    };
    expect(shouldFetchRepositoryTypes(state)).toBe(true);
  });

  it("should not fetch repository types, on pending state", () => {
    const state = {
      pending: {
        [FETCH_REPOSITORY_TYPES]: true
      }
    };
    expect(shouldFetchRepositoryTypes(state)).toBe(false);
  });

  it("should not fetch repository types, on failure state", () => {
    const state = {
      failure: {
        [FETCH_REPOSITORY_TYPES]: new Error("no...")
      }
    };
    expect(shouldFetchRepositoryTypes(state)).toBe(false);
  });

  it("should not fetch repository types, if they are already fetched", () => {
    const state = {
      repositoryTypes: [git, hg, svn]
    };
    expect(shouldFetchRepositoryTypes(state)).toBe(false);
  });
});

describe("repository types fetch", () => {
  const URL = "/api/v2/repositoryTypes";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch repository types", () => {
    fetchMock.getOnce(URL, collection);

    const expectedActions = [
      {
        type: FETCH_REPOSITORY_TYPES_PENDING
      },
      {
        type: FETCH_REPOSITORY_TYPES_SUCCESS,
        payload: collection
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchRepositoryTypesIfNeeded()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_REPOSITORY_TYPES_FAILURE on server error", () => {
    fetchMock.getOnce(URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchRepositoryTypesIfNeeded()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toBe(FETCH_REPOSITORY_TYPES_PENDING);
      expect(actions[1].type).toBe(FETCH_REPOSITORY_TYPES_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should dispatch not dispatch any action, if the repository types are already fetched", () => {
    const store = mockStore({
      repositoryTypes: [git, hg, svn]
    });
    store.dispatch(fetchRepositoryTypesIfNeeded());
    expect(store.getActions().length).toBe(0);
  });
});

describe("repository types reducer", () => {
  it("should return unmodified state on unknown action", () => {
    const state = [];
    expect(reducer(state)).toBe(state);
  });
  it("should store the repository types on FETCH_REPOSITORY_TYPES_SUCCESS", () => {
    const newState = reducer([], fetchRepositoryTypesSuccess(collection));
    expect(newState).toEqual([git, hg, svn]);
  });
});

describe("repository types selectors", () => {
  const error = new Error("The end of the universe");

  it("should return an emtpy array", () => {
    expect(getRepositoryTypes({})).toEqual([]);
  });

  it("should return the repository types", () => {
    const state = {
      repositoryTypes: [git, hg, svn]
    };
    expect(getRepositoryTypes(state)).toEqual([git, hg, svn]);
  });

  it("should return true, when fetch repository types is pending", () => {
    const state = {
      pending: {
        [FETCH_REPOSITORY_TYPES]: true
      }
    };
    expect(isFetchRepositoryTypesPending(state)).toEqual(true);
  });

  it("should return false, when fetch repos is not pending", () => {
    expect(isFetchRepositoryTypesPending({})).toEqual(false);
  });

  it("should return error when fetch repository types did fail", () => {
    const state = {
      failure: {
        [FETCH_REPOSITORY_TYPES]: error
      }
    };
    expect(getFetchRepositoryTypesFailure(state)).toEqual(error);
  });

  it("should return undefined when fetch repos did not fail", () => {
    expect(getFetchRepositoryTypesFailure({})).toBe(undefined);
  });
});
