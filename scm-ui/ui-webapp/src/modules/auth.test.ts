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

import reducer, {
  FETCH_ME,
  FETCH_ME_FAILURE,
  FETCH_ME_PENDING,
  FETCH_ME_SUCCESS,
  FETCH_ME_UNAUTHORIZED,
  fetchMe,
  fetchMeSuccess,
  fetchMeUnauthenticated,
  getFetchMeFailure,
  getLoginFailure,
  getLogoutFailure,
  getMe,
  isAnonymous,
  isAuthenticated,
  isFetchMePending,
  isLoginPending,
  isLogoutPending,
  isRedirecting,
  login,
  LOGIN,
  LOGIN_FAILURE,
  LOGIN_PENDING,
  LOGIN_SUCCESS,
  loginSuccess,
  logout,
  LOGOUT,
  LOGOUT_FAILURE,
  LOGOUT_PENDING,
  LOGOUT_REDIRECT,
  LOGOUT_SUCCESS,
  logoutSuccess,
  redirectAfterLogout
} from "./auth";

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import { FETCH_INDEXRESOURCES_PENDING, FETCH_INDEXRESOURCES_SUCCESS } from "./indexResource";

const me = {
  name: "tricia",
  displayName: "Tricia McMillian",
  mail: "trillian@heartofgold.universe"
};

describe("auth reducer", () => {
  it("should set me and login on successful fetch of me", () => {
    const state = reducer(undefined, fetchMeSuccess(me));
    expect(state.me).toBe(me);
  });

  it("should set authenticated to false", () => {
    const initialState = {
      me
    };
    const state = reducer(initialState, fetchMeUnauthenticated());
    expect(state.me.name).toBeUndefined();
  });

  it("should reset the state after logout", () => {
    const initialState = {
      me
    };
    const state = reducer(initialState, logoutSuccess());
    expect(state.me).toBeUndefined();
    expect(state.authenticated).toBeUndefined();
  });

  it("should keep state and set redirecting to true", () => {
    const initialState = {
      me
    };
    const state = reducer(initialState, redirectAfterLogout());
    expect(state.me).toBe(initialState.me);
    expect(state.redirecting).toBe(true);
  });

  it("should set state authenticated and me after login", () => {
    const state = reducer(undefined, loginSuccess(me));
    expect(state.me).toBe(me);
  });
});

describe("auth actions", () => {
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should dispatch login success and dispatch fetch me", () => {
    fetchMock.postOnce("/api/v2/auth/access_token", {
      body: {
        cookie: true,
        grant_type: "password",
        username: "tricia",
        password: "secret123"
      },
      headers: {
        "content-type": "application/json"
      }
    });

    fetchMock.getOnce("/api/v2/me", {
      body: me,
      headers: {
        "content-type": "application/json"
      }
    });

    const meLink = {
      me: {
        href: "/me"
      }
    };

    fetchMock.getOnce("/api/v2/", {
      _links: meLink
    });

    const expectedActions = [
      {
        type: LOGIN_PENDING
      },
      {
        type: FETCH_INDEXRESOURCES_PENDING
      },
      {
        type: FETCH_INDEXRESOURCES_SUCCESS,
        payload: {
          _links: meLink
        }
      },
      {
        type: LOGIN_SUCCESS,
        payload: me
      }
    ];

    const store = mockStore({});

    return store.dispatch(login("/auth/access_token", "tricia", "secret123")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch login failure", () => {
    fetchMock.postOnce("/api/v2/auth/access_token", {
      status: 400
    });

    const store = mockStore({});
    return store.dispatch(login("/auth/access_token", "tricia", "secret123")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(LOGIN_PENDING);
      expect(actions[1].type).toEqual(LOGIN_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should dispatch fetch me success", () => {
    fetchMock.getOnce("/api/v2/me", {
      body: me,
      headers: {
        "content-type": "application/json"
      }
    });

    const expectedActions = [
      {
        type: FETCH_ME_PENDING
      },
      {
        type: FETCH_ME_SUCCESS,
        payload: me
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchMe("me")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch fetch me failure", () => {
    fetchMock.getOnce("/api/v2/me", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchMe("me")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_ME_PENDING);
      expect(actions[1].type).toEqual(FETCH_ME_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should dispatch fetch me unauthorized", () => {
    fetchMock.getOnce("/api/v2/me", 401);

    const expectedActions = [
      {
        type: FETCH_ME_PENDING
      },
      {
        type: FETCH_ME_UNAUTHORIZED,
        resetPending: true
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchMe("me")).then(() => {
      // return of async actions
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch logout success", () => {
    fetchMock.deleteOnce("/api/v2/auth/access_token", {
      status: 204
    });

    fetchMock.getOnce("/api/v2/me", {
      status: 401
    });

    fetchMock.getOnce("/api/v2/", {
      _links: {
        login: {
          login: "/login"
        }
      }
    });

    const expectedActions = [
      {
        type: LOGOUT_PENDING
      },
      {
        type: LOGOUT_SUCCESS
      },
      {
        type: FETCH_INDEXRESOURCES_PENDING
      }
    ];

    const store = mockStore({});

    return store.dispatch(logout("/auth/access_token")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch logout success and redirect", () => {
    fetchMock.deleteOnce("/api/v2/auth/access_token", {
      status: 200,
      body: {
        logoutRedirect: "http://example.com/cas/logout"
      }
    });

    fetchMock.getOnce("/api/v2/me", {
      status: 401
    });

    fetchMock.getOnce("/api/v2/", {
      _links: {
        login: {
          login: "/login"
        }
      }
    });

    window.location.assign = jest.fn();

    const expectedActions = [
      {
        type: LOGOUT_PENDING
      },
      {
        type: LOGOUT_REDIRECT
      }
    ];

    const store = mockStore({});

    return store.dispatch(logout("/auth/access_token")).then(() => {
      expect(window.location.assign.mock.calls[0][0]).toBe("http://example.com/cas/logout");
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch logout failure", () => {
    fetchMock.deleteOnce("/api/v2/auth/access_token", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(logout("/auth/access_token")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(LOGOUT_PENDING);
      expect(actions[1].type).toEqual(LOGOUT_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("auth selectors", () => {
  const error = new Error("yo it failed");

  it("should return true if me exist and login Link does not exist", () => {
    expect(
      isAuthenticated({
        auth: {
          me
        },
        indexResources: {
          links: {}
        }
      })
    ).toBe(true);
  });

  it("should return me", () => {
    expect(
      getMe({
        auth: {
          me
        }
      })
    ).toBe(me);
  });

  it("should return undefined, if me is not set", () => {
    expect(getMe({})).toBeUndefined();
  });

  it("should return true, if FETCH_ME is pending", () => {
    expect(
      isFetchMePending({
        pending: {
          [FETCH_ME]: true
        }
      })
    ).toBe(true);
  });

  it("should return false, if FETCH_ME is not in pending state", () => {
    expect(
      isFetchMePending({
        pending: {}
      })
    ).toBe(false);
  });

  it("should return true, if LOGIN is pending", () => {
    expect(
      isLoginPending({
        pending: {
          [LOGIN]: true
        }
      })
    ).toBe(true);
  });

  it("should return false, if LOGIN is not in pending state", () => {
    expect(
      isLoginPending({
        pending: {}
      })
    ).toBe(false);
  });

  it("should return true, if LOGOUT is pending", () => {
    expect(
      isLogoutPending({
        pending: {
          [LOGOUT]: true
        }
      })
    ).toBe(true);
  });

  it("should return false, if LOGOUT is not in pending state", () => {
    expect(
      isLogoutPending({
        pending: {}
      })
    ).toBe(false);
  });

  it("should return the error, if failure state is set for FETCH_ME", () => {
    expect(
      getFetchMeFailure({
        failure: {
          [FETCH_ME]: error
        }
      })
    ).toBe(error);
  });

  it("should return unknown, if failure state is not set for FETCH_ME", () => {
    expect(getFetchMeFailure({})).toBeUndefined();
  });

  it("should return the error, if failure state is set for LOGIN", () => {
    expect(
      getLoginFailure({
        failure: {
          [LOGIN]: error
        }
      })
    ).toBe(error);
  });

  it("should return unknown, if failure state is not set for LOGIN", () => {
    expect(getLoginFailure({})).toBeUndefined();
  });

  it("should return the error, if failure state is set for LOGOUT", () => {
    expect(
      getLogoutFailure({
        failure: {
          [LOGOUT]: error
        }
      })
    ).toBe(error);
  });

  it("should return unknown, if failure state is not set for LOGOUT", () => {
    expect(getLogoutFailure({})).toBeUndefined();
  });

  it("should return false, if redirecting is not set", () => {
    expect(isRedirecting({})).toBe(false);
  });

  it("should return false, if redirecting is false", () => {
    expect(
      isRedirecting({
        auth: {
          redirecting: false
        }
      })
    ).toBe(false);
  });

  it("should return true, if redirecting is true", () => {
    expect(
      isRedirecting({
        auth: {
          redirecting: true
        }
      })
    ).toBe(true);
  });
});

it("should check if current user is anonymous", () => {
  expect(isAnonymous({ name: "_anonymous", displayName: "Anon", _links: [], groups: [], mail: "" })).toBeTruthy();
  expect(isAnonymous({ name: "scmadmin", displayName: "SCM Admin", _links: [], groups: [], mail: "" })).toBeFalsy();
});
