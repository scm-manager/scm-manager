// @flow

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  FETCH_CHANGESETS_PENDING,
  FETCH_CHANGESETS_SUCCESS,
  fetchChangesetsByNamespaceAndName,
  fetchChangesetsSuccess, getChangesetsForNameAndNamespaceFromState
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
      {type: FETCH_CHANGESETS_PENDING, payload: {namespace: "foo", name: "bar"}},
      {
        type: FETCH_CHANGESETS_SUCCESS,
        payload: {collection, namespace: "foo", name: "bar"}
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchChangesetsByNamespaceAndName("foo", "bar")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  })
});

describe("changesets reducer", () => {
  const responseBody = {
    _embedded: {
      changesets: [
        {id: "changeset1", author: {mail: "z@phod.com", name: "zaphod"}},
        {id: "changeset2", description: "foo"},
        {id: "changeset3", description: "bar"},
      ],
      _embedded: {
        tags: [],
        branches: [],
        parents: []
      }
    }
  };

  it("should set state to received changesets", () => {
    const newState = reducer({}, fetchChangesetsSuccess(responseBody, "foo", "bar"));
    expect(newState).toBeDefined();
    expect(newState["foo/bar"].byId["changeset1"].author.mail).toEqual("z@phod.com");
    expect(newState["foo/bar"].byId["changeset2"].description).toEqual("foo");
    expect(newState["foo/bar"].byId["changeset3"].description).toEqual("bar");
  });

  it("should not delete existing changesets from state", () => {
    const responseBody = {
      _embedded: {
        changesets: [
          {id: "changeset1", author: {mail: "z@phod.com", name: "zaphod"}},
        ],
        _embedded: {
          tags: [],
          branches: [],
          parents: []
        }
      }
    };
    const newState = reducer({
      "foo/bar": {
        byId: {
          ["changeset2"]: {
            id: "changeset2",
            author: {mail: "mail@author.com", name: "author"}
          }
        }
      }
    }, fetchChangesetsSuccess(responseBody, "foo", "bar"));
    expect(newState["foo/bar"].byId["changeset2"]).toBeDefined();
    expect(newState["foo/bar"].byId["changeset1"]).toBeDefined();
  })
});

describe("changeset selectors", () => {
  it("should get all changesets for a given namespace and name", () => {
    const state = {
      changesets: {
        ["foo/bar"]: {
          byId: {
            "id1": {id: "id1"},
            "id2": {id: "id2"}
          }
        }
      }
    };
    const result = getChangesetsForNameAndNamespaceFromState("foo", "bar", state);
    expect(result).toContainEqual({id: "id1"})
  })
});
