// @flow


import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  FETCH_CHANGESETS_PENDING,
  FETCH_CHANGESETS_SUCCESS,
  fetchChangesets,
  fetchChangesetsSuccess
} from "./changesets";
import reducer from "./changesets";

const collection = {};

describe("fetching of changesets", () => {
  const URL = "/api/rest/v2/repositories/foo/bar/changesets";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should fetch changesets", () => {
    fetchMock.getOnce(URL, "{}");

    const expectedActions = [
      { type: FETCH_CHANGESETS_PENDING },
      {
        type: FETCH_CHANGESETS_SUCCESS,
        payload: collection
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchChangesets("foo", "bar")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  })
});

describe("changesets reducer", () => {
  const responseBody = {
    _embedded: {
      changesets: [
        {id: "changeset1", author: { mail: "z@phod.com", name: "zaphod"}},
        {id: "changeset2"},
        {id: "changeset3"},
      ],
      _embedded: {
        tags: [],
        branches: [],
        parents: []
      }
    }
  };
  it("should set state correctly", () => {
    const newState = reducer({}, fetchChangesetsSuccess(responseBody));
    expect(newState.byIds["changeset1"]).toBeDefined();
    expect(newState.byIds["changeset1"].author.mail).toEqual("z@phod.com");
    expect(newState.byIds["changeset2"]).toBeDefined();
    expect(newState.byIds["changeset3"]).toBeDefined();
  })
});
