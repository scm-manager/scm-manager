import reducer, {
  fetchMeSuccess,
  logout,
  logoutSuccess,
  loginSuccess,
  fetchMeUnauthenticated,
  LOGIN_SUCCESS,
  login,
  LOGIN_FAILURE,
  LOGOUT_FAILURE,
  LOGOUT_SUCCESS,
  FETCH_ME_SUCCESS,
  fetchMe,
  FETCH_ME_FAILURE,
  FETCH_ME_UNAUTHORIZED,
  isAuthenticated,
  LOGIN_PENDING,
  FETCH_ME_PENDING,
  LOGOUT_PENDING,
  getMe,
  isFetchMePending,
  isLoginPending,
  isLogoutPending,
  getFetchMeFailure,
  LOGIN,
  FETCH_ME,
  LOGOUT,
  getLoginFailure,
  getLogoutFailure
} from "./auth";

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

const me = { name: "tricia", displayName: "Tricia McMillian" };

describe("auth reducer", () => {
  it("should set me and login on successful fetch of me", () => {
    const state = reducer(undefined, fetchMeSuccess(me));
    expect(state.me).toBe(me);
    expect(state.authenticated).toBe(true);
  });

  it("should set authenticated to false", () => {
    const initialState = {
      authenticated: true,
      me
    };
    const state = reducer(initialState, fetchMeUnauthenticated());
    expect(state.me.name).toBeUndefined();
    expect(state.authenticated).toBe(false);
  });

  it("should reset the state after logout", () => {
    const initialState = {
      authenticated: true,
      me
    };
    const state = reducer(initialState, logoutSuccess());
    expect(state.me).toBeUndefined();
    expect(state.authenticated).toBeUndefined();
  });

  it("should set state authenticated and me after login", () => {
    const state = reducer(undefined, loginSuccess(me));
    expect(state.me).toBe(me);
    expect(state.authenticated).toBe(true);
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
      body: me,
      headers: { "content-type": "application/json" }
    });

    const expectedActions = [
      { type: LOGIN_PENDING },
      { type: LOGIN_SUCCESS, payload: me }
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

    const store = mockStore({});
    return store.dispatch(login("tricia", "secret123")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(LOGIN_PENDING);
      expect(actions[1].type).toEqual(LOGIN_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should dispatch fetch me success", () => {
    fetchMock.getOnce("/scm/api/rest/v2/me", {
      body: me,
      headers: { "content-type": "application/json" }
    });

    const expectedActions = [
      { type: FETCH_ME_PENDING },
      {
        type: FETCH_ME_SUCCESS,
        payload: me
      }
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
      expect(actions[0].type).toEqual(FETCH_ME_PENDING);
      expect(actions[1].type).toEqual(FETCH_ME_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should dispatch fetch me unauthorized", () => {
    fetchMock.getOnce("/scm/api/rest/v2/me", {
      status: 401
    });

    const expectedActions = [
      { type: FETCH_ME_PENDING },
      { type: FETCH_ME_UNAUTHORIZED, resetPending: true }
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
      { type: LOGOUT_PENDING },
      { type: LOGOUT_SUCCESS }
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

    const store = mockStore({});
    return store.dispatch(logout()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(LOGOUT_PENDING);
      expect(actions[1].type).toEqual(LOGOUT_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("auth selectors", () => {
  const error = new Error("yo it failed");

  it("should be false, if authenticated is undefined or false", () => {
    expect(isAuthenticated({})).toBe(false);
    expect(isAuthenticated({ auth: {} })).toBe(false);
    expect(isAuthenticated({ auth: { authenticated: false } })).toBe(false);
  });

  it("should be true, if authenticated is true", () => {
    expect(isAuthenticated({ auth: { authenticated: true } })).toBe(true);
  });

  it("should return me", () => {
    expect(getMe({ auth: { me } })).toBe(me);
  });

  it("should return undefined, if me is not set", () => {
    expect(getMe({})).toBeUndefined();
  });

  it("should return true, if FETCH_ME is pending", () => {
    expect(isFetchMePending({ pending: { [FETCH_ME]: true } })).toBe(true);
  });

  it("should return false, if FETCH_ME is not in pending state", () => {
    expect(isFetchMePending({ pending: {} })).toBe(false);
  });

  it("should return true, if LOGIN is pending", () => {
    expect(isLoginPending({ pending: { [LOGIN]: true } })).toBe(true);
  });

  it("should return false, if LOGIN is not in pending state", () => {
    expect(isLoginPending({ pending: {} })).toBe(false);
  });

  it("should return true, if LOGOUT is pending", () => {
    expect(isLogoutPending({ pending: { [LOGOUT]: true } })).toBe(true);
  });

  it("should return false, if LOGOUT is not in pending state", () => {
    expect(isLogoutPending({ pending: {} })).toBe(false);
  });

  it("should return the error, if failure state is set for FETCH_ME", () => {
    expect(getFetchMeFailure({ failure: { [FETCH_ME]: error } })).toBe(error);
  });

  it("should return unknown, if failure state is not set for FETCH_ME", () => {
    expect(getFetchMeFailure({})).toBeUndefined();
  });

  it("should return the error, if failure state is set for LOGIN", () => {
    expect(getLoginFailure({ failure: { [LOGIN]: error } })).toBe(error);
  });

  it("should return unknown, if failure state is not set for LOGIN", () => {
    expect(getLoginFailure({})).toBeUndefined();
  });

  it("should return the error, if failure state is set for LOGOUT", () => {
    expect(getLogoutFailure({ failure: { [LOGOUT]: error } })).toBe(error);
  });

  it("should return unknown, if failure state is not set for LOGOUT", () => {
    expect(getLogoutFailure({})).toBeUndefined();
  });
});
