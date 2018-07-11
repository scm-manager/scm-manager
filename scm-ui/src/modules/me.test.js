// @flow
import reducer, {
  ME_AUTHENTICATED_REQUEST,
  ME_AUTHENTICATED_SUCCESS,
  ME_AUTHENTICATED_FAILURE,
  ME_UNAUTHENTICATED,
  fetchMe
} from "./me";

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

describe("fetch tests", () => {
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  test("successful me fetch", () => {
    fetchMock.getOnce("/scm/api/rest/v2/me", {
      body: { username: "sorbot" },
      headers: { "content-type": "application/json" }
    });

    const expectedActions = [
      { type: ME_AUTHENTICATED_REQUEST },
      { type: ME_AUTHENTICATED_SUCCESS, payload: { username: "sorbot" } }
    ];

    const store = mockStore({});

    return store.dispatch(fetchMe()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  test("me fetch failed", () => {
    fetchMock.getOnce("/scm/api/rest/v2/me", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchMe()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(ME_AUTHENTICATED_REQUEST);
      expect(actions[1].type).toEqual(ME_AUTHENTICATED_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  test("me fetch unauthenticated", () => {
    fetchMock.getOnce("/scm/api/rest/v2/me", {
      status: 401
    });

    const expectedActions = [
      { type: ME_AUTHENTICATED_REQUEST },
      { type: ME_UNAUTHENTICATED }
    ];

    const store = mockStore({});

    return store.dispatch(fetchMe()).then(() => {
      // return of async actions
      expect(store.getActions()).toEqual(expectedActions);
    });
  });
});

describe("reducer tests", () => {
  test("me request", () => {
    var newState = reducer({}, { type: ME_AUTHENTICATED_REQUEST });
    expect(newState.loading).toBeTruthy();
    expect(newState.me).toBeNull();
    expect(newState.error).toBeNull();
  });

  test("fetch me successful", () => {
    const me = { username: "tricia" };
    var newState = reducer({}, { type: ME_AUTHENTICATED_SUCCESS, payload: me });
    expect(newState.loading).toBeFalsy();
    expect(newState.me).toBe(me);
    expect(newState.error).toBe(null);
  });

  test("fetch me failed", () => {
    const err = new Error("error!");
    var newState = reducer(
      {},
      { type: ME_AUTHENTICATED_FAILURE, payload: err }
    );
    expect(newState.loading).toBeFalsy();
    expect(newState.me).toBeNull();
    expect(newState.error).toBe(err);
  });

  test("me unauthenticated", () => {
    var newState = reducer({}, { type: ME_UNAUTHENTICATED });
    expect(newState.loading).toBeFalsy();
    expect(newState.me).toBeNull();
    expect(newState.error).toBeNull();
  });
});
