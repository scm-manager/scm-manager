import reducer, {
  fetchMeSuccess,
  logout,
  logoutSuccess,
  loginSuccess,
  fetchMeRequest,
  loginRequest,
  logoutRequest,
  fetchMeFailure,
  fetchMeUnauthenticated,
  loginFailure,
  logoutFailure,
  LOGIN_REQUEST,
  FETCH_ME_REQUEST,
  LOGIN_SUCCESS,
  login,
  LOGIN_FAILURE,
  LOGOUT_FAILURE,
  FETCH_ME_SUCCESS,
  fetchMe,
  FETCH_ME_FAILURE,
  FETCH_ME_UNAUTHORIZED,
  isAuthenticated
} from "./auth";

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import { LOGOUT_REQUEST, LOGOUT_SUCCESS } from "./auth";

describe("auth reducer", () => {
  it("should initialize in loading state ", () => {
    const state = reducer();
    expect(state.me.loading).toBeTruthy();
  });

  it("should set me and login on successful fetch of me", () => {
    const state = reducer(undefined, fetchMeSuccess({ username: "tricia" }));
    expect(state.me.loading).toBeFalsy();
    expect(state.me.entry.username).toBe("tricia");
    expect(state.login.authenticated).toBeTruthy();
  });

  it("should set authenticated to false", () => {
    const initialState = {
      login: {
        authenticated: true
      },
      me: {
        username: "tricia"
      }
    };
    const state = reducer(initialState, fetchMeUnauthenticated());
    expect(state.me.username).toBeUndefined();
    expect(state.login.authenticated).toBeFalsy();
  });

  it("should reset the state after logout", () => {
    const initialState = {
      login: {
        authenticated: true
      },
      me: {
        username: "tricia"
      }
    };
    const state = reducer(initialState, logoutSuccess());
    expect(state.me.loading).toBeTruthy();
    expect(state.me.entry).toBeFalsy();
    expect(state.login).toBeUndefined();
  });

  it("should set state authenticated after login", () => {
    const state = reducer(undefined, loginSuccess());
    expect(state.login.authenticated).toBeTruthy();
  });

  it("should set me to loading", () => {
    const state = reducer({ me: { loading: false } }, fetchMeRequest());
    expect(state.me.loading).toBeTruthy();
  });

  it("should set login to loading", () => {
    const state = reducer({ login: { loading: false } }, loginRequest());
    expect(state.login.loading).toBeTruthy();
  });

  it("should set logout to loading", () => {
    const state = reducer({ logout: { loading: false } }, logoutRequest());
    expect(state.logout.loading).toBeTruthy();
  });

  it("should set me to error", () => {
    const error = new Error("failed");
    const state = reducer(undefined, fetchMeFailure(error));
    expect(state.me.error).toBe(error);
  });

  it("should set login to error", () => {
    const error = new Error("failed");
    const state = reducer(undefined, loginFailure(error));
    expect(state.login.error).toBe(error);
  });

  it("should set logout to error", () => {
    const error = new Error("failed");
    const state = reducer(undefined, logoutFailure(error));
    expect(state.logout.error).toBe(error);
  });
});

describe("auth actions", () => {
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should dispatch login success and dispatch fetch me", () => {
    fetchMock.postOnce("/scm/api/rest/v2/auth/access_token", {
      body: {
        cookie: true,
        grant_type: "password",
        username: "tricia",
        password: "secret123"
      },
      headers: { "content-type": "application/json" }
    });

    fetchMock.getOnce("/scm/api/rest/v2/me", {
      body: {
        username: "tricia"
      },
      headers: { "content-type": "application/json" }
    });

    const expectedActions = [
      { type: LOGIN_REQUEST },
      { type: FETCH_ME_REQUEST },
      { type: LOGIN_SUCCESS }
    ];

    const store = mockStore({});

    return store.dispatch(login("tricia", "secret123")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch login failure", () => {
    fetchMock.postOnce("/scm/api/rest/v2/auth/access_token", {
      status: 400
    });

    const expectedActions = [{ type: LOGIN_REQUEST }, { type: LOGIN_FAILURE }];

    const store = mockStore({});
    return store.dispatch(login("tricia", "secret123")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(LOGIN_REQUEST);
      expect(actions[1].type).toEqual(LOGIN_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should dispatch fetch me success", () => {
    fetchMock.getOnce("/scm/api/rest/v2/me", {
      body: { username: "sorbot" },
      headers: { "content-type": "application/json" }
    });

    const expectedActions = [
      { type: FETCH_ME_REQUEST },
      { type: FETCH_ME_SUCCESS, payload: { username: "sorbot" } }
    ];

    const store = mockStore({});

    return store.dispatch(fetchMe()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch fetch me failure", () => {
    fetchMock.getOnce("/scm/api/rest/v2/me", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchMe()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_ME_REQUEST);
      expect(actions[1].type).toEqual(FETCH_ME_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should dispatch fetch me unauthorized", () => {
    fetchMock.getOnce("/scm/api/rest/v2/me", {
      status: 401
    });

    const expectedActions = [
      { type: FETCH_ME_REQUEST },
      { type: FETCH_ME_UNAUTHORIZED }
    ];

    const store = mockStore({});

    return store.dispatch(fetchMe()).then(() => {
      // return of async actions
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch logout success", () => {
    fetchMock.deleteOnce("/scm/api/rest/v2/auth/access_token", {
      status: 204
    });

    fetchMock.getOnce("/scm/api/rest/v2/me", {
      status: 401
    });

    const expectedActions = [
      { type: LOGOUT_REQUEST },
      { type: LOGOUT_SUCCESS },
      { type: FETCH_ME_REQUEST }
    ];

    const store = mockStore({});

    return store.dispatch(logout()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch logout failure", () => {
    fetchMock.deleteOnce("/scm/api/rest/v2/auth/access_token", {
      status: 500
    });

    const expectedActions = [
      { type: LOGOUT_REQUEST },
      { type: LOGOUT_FAILURE }
    ];

    const store = mockStore({});
    return store.dispatch(logout()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(LOGOUT_REQUEST);
      expect(actions[1].type).toEqual(LOGOUT_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("auth selectors", () => {
  it("should be false", () => {
    expect(isAuthenticated({})).toBeFalsy();
    expect(isAuthenticated({ auth: {} })).toBeFalsy();
    expect(isAuthenticated({ auth: { login: {} } })).toBeFalsy();
    expect(
      isAuthenticated({ auth: { login: { authenticated: false } } })
    ).toBeFalsy();
  });

  it("shuld be true", () => {
    expect(
      isAuthenticated({ auth: { login: { authenticated: true } } })
    ).toBeTruthy();
  });
});
