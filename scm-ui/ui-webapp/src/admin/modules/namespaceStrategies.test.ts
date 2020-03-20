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
import {
  default as reducer,
  FETCH_NAMESPACESTRATEGIES_TYPES,
  FETCH_NAMESPACESTRATEGIES_TYPES_FAILURE,
  FETCH_NAMESPACESTRATEGIES_TYPES_PENDING,
  FETCH_NAMESPACESTRATEGIES_TYPES_SUCCESS,
  fetchNamespaceStrategiesIfNeeded,
  fetchNamespaceStrategiesSuccess,
  getFetchNamespaceStrategiesFailure,
  getNamespaceStrategies,
  isFetchNamespaceStrategiesPending,
  shouldFetchNamespaceStrategies
} from "./namespaceStrategies";
import { MODIFY_CONFIG_SUCCESS } from "./config";

const strategies = {
  current: "UsernameNamespaceStrategy",
  available: [
    "UsernameNamespaceStrategy",
    "CustomNamespaceStrategy",
    "CurrentYearNamespaceStrategy",
    "RepositoryTypeNamespaceStrategy"
  ],
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/namespaceStrategies"
    }
  }
};

describe("namespace strategy caching", () => {
  it("should fetch strategies, on empty state", () => {
    expect(shouldFetchNamespaceStrategies({})).toBe(true);
  });

  it("should fetch strategies, on empty namespaceStrategies node", () => {
    const state = {
      namespaceStrategies: {}
    };
    expect(shouldFetchNamespaceStrategies(state)).toBe(true);
  });

  it("should not fetch strategies, on pending state", () => {
    const state = {
      pending: {
        [FETCH_NAMESPACESTRATEGIES_TYPES]: true
      }
    };
    expect(shouldFetchNamespaceStrategies(state)).toBe(false);
  });

  it("should not fetch strategies, on failure state", () => {
    const state = {
      failure: {
        [FETCH_NAMESPACESTRATEGIES_TYPES]: new Error("no...")
      }
    };
    expect(shouldFetchNamespaceStrategies(state)).toBe(false);
  });

  it("should not fetch strategies, if they are already fetched", () => {
    const state = {
      namespaceStrategies: {
        current: "some"
      }
    };
    expect(shouldFetchNamespaceStrategies(state)).toBe(false);
  });
});

describe("namespace strategies fetch", () => {
  const URL = "http://scm.hitchhiker.com/api/v2/namespaceStrategies";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  const createStore = (initialState = {}) => {
    return mockStore({
      ...initialState,
      indexResources: {
        links: {
          namespaceStrategies: {
            href: URL
          }
        }
      }
    });
  };

  it("should successfully fetch strategies", () => {
    fetchMock.getOnce(URL, strategies);

    const expectedActions = [
      {
        type: FETCH_NAMESPACESTRATEGIES_TYPES_PENDING
      },
      {
        type: FETCH_NAMESPACESTRATEGIES_TYPES_SUCCESS,
        payload: strategies
      }
    ];

    const store = createStore();
    return store.dispatch(fetchNamespaceStrategiesIfNeeded()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_NAMESPACESTRATEGIES_TYPES_FAILURE on server error", () => {
    fetchMock.getOnce(URL, {
      status: 500
    });

    const store = createStore();
    return store.dispatch(fetchNamespaceStrategiesIfNeeded()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toBe(FETCH_NAMESPACESTRATEGIES_TYPES_PENDING);
      expect(actions[1].type).toBe(FETCH_NAMESPACESTRATEGIES_TYPES_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should not dispatch any action, if the strategies are already fetched", () => {
    const store = createStore({
      namespaceStrategies: strategies
    });
    store.dispatch(fetchNamespaceStrategiesIfNeeded());
    expect(store.getActions().length).toBe(0);
  });
});

describe("namespace strategies reducer", () => {
  it("should return unmodified state on unknown action", () => {
    const state = {};
    expect(reducer(state)).toBe(state);
  });

  it("should store the strategies on success", () => {
    const newState = reducer({}, fetchNamespaceStrategiesSuccess(strategies));
    expect(newState).toBe(strategies);
  });

  it("should clear store if config was modified", () => {
    const modifyConfigAction = {
      type: MODIFY_CONFIG_SUCCESS,
      payload: {
        namespaceStrategy: "CustomNamespaceStrategy"
      }
    };
    const newState = reducer(strategies, modifyConfigAction);
    expect(newState.current).toEqual("CustomNamespaceStrategy");
  });
});

describe("namespace strategy selectors", () => {
  const error = new Error("The end of the universe");

  it("should return an empty object", () => {
    expect(getNamespaceStrategies({})).toEqual({});
  });

  it("should return the namespace strategies", () => {
    const state = {
      namespaceStrategies: strategies
    };
    expect(getNamespaceStrategies(state)).toBe(strategies);
  });

  it("should return true, when fetch namespace strategies is pending", () => {
    const state = {
      pending: {
        [FETCH_NAMESPACESTRATEGIES_TYPES]: true
      }
    };
    expect(isFetchNamespaceStrategiesPending(state)).toEqual(true);
  });

  it("should return false, when fetch strategies is not pending", () => {
    expect(isFetchNamespaceStrategiesPending({})).toEqual(false);
  });

  it("should return error when fetch namespace strategies did fail", () => {
    const state = {
      failure: {
        [FETCH_NAMESPACESTRATEGIES_TYPES]: error
      }
    };
    expect(getFetchNamespaceStrategiesFailure(state)).toEqual(error);
  });

  it("should return undefined when fetch strategies did not fail", () => {
    expect(getFetchNamespaceStrategiesFailure({})).toBe(undefined);
  });
});
