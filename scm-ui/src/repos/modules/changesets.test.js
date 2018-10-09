// @flow

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  FETCH_CHANGESETS,
  FETCH_CHANGESETS_FAILURE,
  FETCH_CHANGESETS_PENDING,
  FETCH_CHANGESETS_SUCCESS,
  FETCH_CHANGESET,
  FETCH_CHANGESET_FAILURE,
  FETCH_CHANGESET_PENDING,
  FETCH_CHANGESET_SUCCESS,
  fetchChangesets,
  fetchChangesetsByBranchAndPage,
  fetchChangesetsByNamespaceNameAndBranch,
  fetchChangesetsByPage,
  fetchChangesetsSuccess,
  getChangesets,
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  fetchChangeset,
  getChangeset,
  fetchChangesetIfNeeded,
  shouldFetchChangeset,
  isFetchChangesetPending,
  getFetchChangesetFailure,
  fetchChangesetSuccess
} from "./changesets";
import reducer from "./changesets";

const changesets = {};

describe("changesets", () => {
  describe("fetching of changesets", () => {
    const DEFAULT_BRANCH_URL = "/api/v2/repositories/foo/bar/changesets";
    const SPECIFIC_BRANCH_URL =
      "/api/v2/repositories/foo/bar/branches/specific/changesets";
    const mockStore = configureMockStore([thunk]);

    afterEach(() => {
      fetchMock.reset();
      fetchMock.restore();
    });

    //********added for detailed view of changesets
    const changesetId = "aba876c0625d90a6aff1494f3d161aaa7008b958";

    it("should fetch changeset", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "/" + changesetId, "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESET_PENDING,
          payload: {
            id: changesetId,
            namespace: "foo",
            repoName: "bar"
          },
          itemId: "foo/bar/" + changesetId
        },
        {
          type: FETCH_CHANGESET_SUCCESS,
          payload: {
            changeset: {},
            id: changesetId,
            namespace: "foo",
            repoName: "bar"
          },
          itemId: "foo/bar/" + changesetId
        }
      ];

      const store = mockStore({});
      return store
        .dispatch(fetchChangeset("foo", "bar", changesetId))
        .then(() => {
          expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it("should fail fetching changeset on error", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "/" + changesetId, 500);

      const expectedActions = [
        {
          type: FETCH_CHANGESET_PENDING,
          payload: {
            id: changesetId,
            namespace: "foo",
            repoName: "bar"
          },
          itemId: "foo/bar/" + changesetId
        }
      ];

      const store = mockStore({});
      return store
        .dispatch(fetchChangeset("foo", "bar", changesetId))
        .then(() => {
          expect(store.getActions()[0]).toEqual(expectedActions[0]);
          expect(store.getActions()[1].type).toEqual(FETCH_CHANGESET_FAILURE);
          expect(store.getActions()[1].payload).toBeDefined();
        });
    });

    it("should fetch changeset if needed", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "/" + "id3", "{}");

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

      const expectedActions = [
        {
          type: FETCH_CHANGESET_PENDING,
          payload: {
            id: "id3",
            namespace: "foo",
            repoName: "bar"
          },
          itemId: "foo/bar/" + "id3"
        },
        {
          type: FETCH_CHANGESET_SUCCESS,
          payload: {
            changeset: {},
            id: "id3",
            namespace: "foo",
            repoName: "bar"
          },
          itemId: "foo/bar/" + "id3"
        }
      ];

      const store = mockStore({});
      return store
        .dispatch(fetchChangesetIfNeeded("foo", "bar", "id3"))
        .then(() => {
          expect(store.getActions()).toEqual(expectedActions);
        });
    });

    it("should not fetch changeset if not needed", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "/" + "id1", 500);

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

      const expectedActions = [];

      const store = mockStore(state);
      return expect(
        store.dispatch(fetchChangesetIfNeeded("foo", "bar", "id1"))
      ).toEqual(undefined);
    });

    //********end of added for detailed view of changesets

    it("should fetch changesets for default branch", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL, "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: "foo/bar",
          itemId: "foo/bar"
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: changesets,
          itemId: "foo/bar"
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesets("foo", "bar")).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should fetch changesets for specific branch", () => {
      const itemId = "foo/bar/specific";
      fetchMock.getOnce(SPECIFIC_BRANCH_URL, "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: itemId,
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
        .dispatch(
          fetchChangesetsByNamespaceNameAndBranch("foo", "bar", "specific")
        )
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
          payload: itemId,
          itemId
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesets("foo", "bar")).then(() => {
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
          payload: itemId,
          itemId
        }
      ];

      const store = mockStore({});
      return store
        .dispatch(
          fetchChangesetsByNamespaceNameAndBranch("foo", "bar", "specific")
        )
        .then(() => {
          expect(store.getActions()[0]).toEqual(expectedActions[0]);
          expect(store.getActions()[1].type).toEqual(FETCH_CHANGESETS_FAILURE);
          expect(store.getActions()[1].payload).toBeDefined();
        });
    });

    it("should fetch changesets by page", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "?page=5", "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: "foo/bar",
          itemId: "foo/bar"
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: changesets,
          itemId: "foo/bar"
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesetsByPage("foo", "bar", 5)).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should fetch changesets by branch and page", () => {
      fetchMock.getOnce(SPECIFIC_BRANCH_URL + "?page=5", "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          payload: "foo/bar/specific",
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
        .dispatch(fetchChangesetsByBranchAndPage("foo", "bar", "specific", 5))
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
        fetchChangesetsSuccess(responseBody, "foo", "bar")
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
      expect(newState.list).toEqual({
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
        fetchChangesetsSuccess(responseBody, "foo", "bar")
      );
      expect(newState.byKey["foo/bar"].byId["changeset2"]).toBeDefined();
      expect(newState.byKey["foo/bar"].byId["changeset1"]).toBeDefined();
    });

    //********added for detailed view of changesets
    const responseBodySingleChangeset = {
      id: "id3",
      author: {
        mail: "z@phod.com",
        name: "zaphod"
      },
      date: "2018-09-13T08:46:22Z",
      description: "added testChangeset",
      _links: {},
      _embedded: {
        tags: [],
        branches: []
      }
    };

    it("should add changeset to state", () => {
      const newState = reducer(
        {
          byKey: {
            "foo/bar": {
              byId: {
                ["id2"]: {
                  id: "id2",
                  author: { mail: "mail@author.com", name: "author" }
                }
              }
            }
          },
          list: {
            entry: {
              page: 1,
              pageTotal: 10,
              _links: {}
            },
            entries: ["id2"]
          }
        },
        fetchChangesetSuccess(responseBodySingleChangeset, "foo", "bar", "id3")
      );
      expect(newState).toBeDefined();
      expect(newState.byKey["foo/bar"].byId["id3"].description).toEqual(
        "added testChangeset"
      );
      expect(newState.byKey["foo/bar"].byId["id3"].author.mail).toEqual(
        "z@phod.com"
      );
      expect(newState.byKey["foo/bar"].byId["id2"]).toBeDefined();
      expect(newState.byKey["foo/bar"].byId["id3"]).toBeDefined();
      expect(newState.list).toEqual({
        entry: {
          page: 1,
          pageTotal: 10,
          _links: {}
        },
        entries: ["id2", "id3"]
      });
    });
    //********end of added for detailed view of changesets
  });

  describe("changeset selectors", () => {
    const error = new Error("Something went wrong");

    //********added for detailed view of changesets

    it("should return changeset", () => {
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
      const result = getChangeset(state, "foo", "bar", "id1");
      expect(result).toEqual({ id: "id1" });
    });

    it("should return null if changeset does not exist", () => {
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
      const result = getChangeset(state, "foo", "bar", "id3");
      expect(result).toEqual(null);
    });

    it("should return true if changeset does not exist", () => {
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
      const result = shouldFetchChangeset(state, "foo", "bar", "id3");
      expect(result).toEqual(true);
    });

    it("should return false if changeset exists", () => {
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
      const result = shouldFetchChangeset(state, "foo", "bar", "id2");
      expect(result).toEqual(false);
    });

    it("should return true, when fetching changeset is pending", () => {
      const state = {
        pending: {
          [FETCH_CHANGESET + "/foo/bar/id1"]: true
        }
      };

      expect(isFetchChangesetPending(state, "foo", "bar", "id1")).toBeTruthy();
    });

    it("should return false, when fetching changeset is not pending", () => {
      expect(isFetchChangesetPending({}, "foo", "bar", "id1")).toEqual(false);
    });

    it("should return error if fetching changeset failed", () => {
      const state = {
        failure: {
          [FETCH_CHANGESET + "/foo/bar/id1"]: error
        }
      };

      expect(getFetchChangesetFailure(state, "foo", "bar", "id1")).toEqual(
        error
      );
    });

    it("should return false if fetching changeset did not fail", () => {
      expect(getFetchChangesetFailure({}, "foo", "bar", "id1")).toBeUndefined();
    });
    //********end of added for detailed view of changesets

    it("should get all changesets for a given namespace and name", () => {
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
      const result = getChangesets(state, "foo", "bar");
      expect(result).toContainEqual({ id: "id1" });
    });

    it("should return true, when fetching changesets is pending", () => {
      const state = {
        pending: {
          [FETCH_CHANGESETS + "/foo/bar"]: true
        }
      };

      expect(isFetchChangesetsPending(state, "foo", "bar")).toBeTruthy();
    });

    it("should return false, when fetching changesets is not pending", () => {
      expect(isFetchChangesetsPending({}, "foo", "bar")).toEqual(false);
    });

    it("should return error if fetching changesets failed", () => {
      const state = {
        failure: {
          [FETCH_CHANGESETS + "/foo/bar"]: error
        }
      };

      expect(getFetchChangesetsFailure(state, "foo", "bar")).toEqual(error);
    });

    it("should return false if fetching changesets did not fail", () => {
      expect(getFetchChangesetsFailure({}, "foo", "bar")).toBeUndefined();
    });
  });
});
