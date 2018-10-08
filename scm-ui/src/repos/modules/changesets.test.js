// @flow

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  FETCH_CHANGESETS,
  FETCH_CHANGESETS_FAILURE,
  FETCH_CHANGESETS_PENDING,
  FETCH_CHANGESETS_SUCCESS,
  fetchChangesets,
  fetchChangesetsByBranchAndPage,
  fetchChangesetsByBranch,
  fetchChangesetsByPage,
  fetchChangesetsSuccess,
  getChangesets,
  getFetchChangesetsFailure,
  isFetchChangesetsPending
} from "./changesets";
import reducer from "./changesets";
const branch = {
  name: "specific",
  revision: "123",
  _links: {
    history: {
      href:
        "http://scm/api/rest/v2/repositories/foo/bar/branches/specific/changesets"
    }
  }
};

const repository = {
  namespace: "foo",
  name: "bar",
  type: "GIT",
  _links: {
    self: {
      href: "http://scm/api/rest/v2/repositories/foo/bar"
    },
    changesets: {
      href: "http://scm/api/rest/v2/repositories/foo/bar/changesets"
    },
    branches: [branch]
  }
};

const changesets = {};

describe("changesets", () => {
  describe("fetching of changesets", () => {
    const DEFAULT_BRANCH_URL =
      "http://scm/api/rest/v2/repositories/foo/bar/changesets";
    const SPECIFIC_BRANCH_URL =
      "http://scm/api/rest/v2/repositories/foo/bar/branches/specific/changesets";
    const mockStore = configureMockStore([thunk]);

    afterEach(() => {
      fetchMock.reset();
      fetchMock.restore();
    });

    it("should fetch changesets for default branch", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL, "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: { repository },
          itemId: "foo/bar"
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: changesets,
          itemId: "foo/bar"
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesets(repository)).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should fetch changesets for specific branch", () => {
      const itemId = "foo/bar/specific";
      fetchMock.getOnce(SPECIFIC_BRANCH_URL, "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: { repository, branch },
          itemId
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: changesets,
          itemId
        }
      ];

      const store = mockStore({});
      return store
        .dispatch(fetchChangesetsByBranch(repository, branch))
        .then(() => {
          expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it("should fail fetching changesets on error", () => {
      const itemId = "foo/bar";
      fetchMock.getOnce(DEFAULT_BRANCH_URL, 500);

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: { repository },
          itemId
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesets(repository)).then(() => {
        expect(store.getActions()[0]).toEqual(expectedActions[0]);
        expect(store.getActions()[1].type).toEqual(FETCH_CHANGESETS_FAILURE);
        expect(store.getActions()[1].payload).toBeDefined();
      });
    });

    it("should fail fetching changesets for specific branch on error", () => {
      const itemId = "foo/bar/specific";
      fetchMock.getOnce(SPECIFIC_BRANCH_URL, 500);

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: { repository, branch },
          itemId
        }
      ];

      const store = mockStore({});
      return store
        .dispatch(fetchChangesetsByBranch(repository, branch))
        .then(() => {
          expect(store.getActions()[0]).toEqual(expectedActions[0]);
          expect(store.getActions()[1].type).toEqual(FETCH_CHANGESETS_FAILURE);
          expect(store.getActions()[1].payload).toBeDefined();
        });
    });

    it("should fetch changesets by page", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "?page=4", "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: { repository },
          itemId: "foo/bar"
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: changesets,
          itemId: "foo/bar"
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesetsByPage(repository, 5)).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should fetch changesets by branch and page", () => {
      fetchMock.getOnce(SPECIFIC_BRANCH_URL + "?page=4", "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: { repository, branch },
          itemId: "foo/bar/specific"
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: changesets,
          itemId: "foo/bar/specific"
        }
      ];

      const store = mockStore({});
      return store
        .dispatch(fetchChangesetsByBranchAndPage(repository, branch, 5))
        .then(() => {
          expect(store.getActions()).toEqual(expectedActions);
        });
    });
  });

  describe("changesets reducer", () => {
    const responseBody = {
      page: 1,
      pageTotal: 10,
      _links: {},
      _embedded: {
        changesets: [
          { id: "changeset1", author: { mail: "z@phod.com", name: "zaphod" } },
          { id: "changeset2", description: "foo" },
          { id: "changeset3", description: "bar" }
        ],
        _embedded: {
          tags: [],
          branches: [],
          parents: []
        }
      }
    };

    it("should set state to received changesets", () => {
      const newState = reducer(
        {},
        fetchChangesetsSuccess(responseBody, repository)
      );
      expect(newState).toBeDefined();
      expect(newState.byKey["foo/bar"].byId["changeset1"].author.mail).toEqual(
        "z@phod.com"
      );
      expect(newState.byKey["foo/bar"].byId["changeset2"].description).toEqual(
        "foo"
      );
      expect(newState.byKey["foo/bar"].byId["changeset3"].description).toEqual(
        "bar"
      );
      expect(newState.byKey["foo/bar"].list).toEqual({
        entry: {
          page: 1,
          pageTotal: 10,
          _links: {}
        },
        entries: ["changeset1", "changeset2", "changeset3"]
      });
    });

    it("should not delete existing changesets from state", () => {
      const responseBody = {
        _embedded: {
          changesets: [
            { id: "changeset1", author: { mail: "z@phod.com", name: "zaphod" } }
          ],
          _embedded: {
            tags: [],
            branches: [],
            parents: []
          }
        }
      };
      const newState = reducer(
        {
          byKey: {
            "foo/bar": {
              byId: {
                ["changeset2"]: {
                  id: "changeset2",
                  author: { mail: "mail@author.com", name: "author" }
                }
              }
            }
          }
        },
        fetchChangesetsSuccess(responseBody, repository)
      );
      expect(newState.byKey["foo/bar"].byId["changeset2"]).toBeDefined();
      expect(newState.byKey["foo/bar"].byId["changeset1"]).toBeDefined();
    });
  });

  describe("changeset selectors", () => {
    const error = new Error("Something went wrong");

    it("should get all changesets for a given repository", () => {
      const state = {
        changesets: {
          byKey: {
            "foo/bar": {
              byId: {
                id1: { id: "id1" },
                id2: { id: "id2" }
              }
            }
          }
        }
      };
      const result = getChangesets(state, repository);
      expect(result).toContainEqual({ id: "id1" });
    });

    it("should return true, when fetching changesets is pending", () => {
      const state = {
        pending: {
          [FETCH_CHANGESETS + "/foo/bar"]: true
        }
      };

      expect(isFetchChangesetsPending(state, repository)).toBeTruthy();
    });

    it("should return false, when fetching changesets is not pending", () => {
      expect(isFetchChangesetsPending({}, repository)).toEqual(false);
    });

    it("should return error if fetching changesets failed", () => {
      const state = {
        failure: {
          [FETCH_CHANGESETS + "/foo/bar"]: error
        }
      };

      expect(getFetchChangesetsFailure(state, repository)).toEqual(error);
    });

    it("should return false if fetching changesets did not fail", () => {
      expect(getFetchChangesetsFailure({}, repository)).toBeUndefined();
    });
  });
});
