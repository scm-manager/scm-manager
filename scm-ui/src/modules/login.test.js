// @flow
import reducer, {
  login,
  LOGIN_REQUEST,
  LOGIN_FAILED,
  LOGIN_SUCCESSFUL
} from "./login";

import { ME_AUTHENTICATED_REQUEST, ME_AUTHENTICATED_SUCCESS } from "./me";

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

describe("action tests", () => {
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  test("login success", () => {
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
      { type: ME_AUTHENTICATED_REQUEST },
      { type: LOGIN_SUCCESSFUL }
    ];

    const store = mockStore({});

    return store.dispatch(login("tricia", "secret123")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  test("login failed", () => {
    fetchMock.postOnce("/scm/api/rest/v2/auth/access_token", {
      status: 400
    });

    const expectedActions = [{ type: LOGIN_REQUEST }, { type: LOGIN_FAILED }];

    const store = mockStore({});
    return store.dispatch(login("tricia", "secret123")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(LOGIN_REQUEST);
      expect(actions[1].type).toEqual(LOGIN_FAILED);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("reducer tests", () => {
  test("login request", () => {
    var newState = reducer({}, { type: LOGIN_REQUEST });
    expect(newState.loading).toBeTruthy();
    expect(newState.login).toBeFalsy();
    expect(newState.error).toBeNull();
  });

  test("login successful", () => {
    var newState = reducer({ login: false }, { type: LOGIN_SUCCESSFUL });
    expect(newState.loading).toBeFalsy();
    expect(newState.login).toBeTruthy();
    expect(newState.error).toBe(null);
  });

  test("login failed", () => {
    const err = new Error("error!");
    var newState = reducer({}, { type: LOGIN_FAILED, payload: err });
    expect(newState.loading).toBeFalsy();
    expect(newState.login).toBeFalsy();
    expect(newState.error).toBe(err);
  });
});
