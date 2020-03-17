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

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  FETCH_CHANGESET,
  FETCH_CHANGESET_FAILURE,
  FETCH_CHANGESET_PENDING,
  FETCH_CHANGESET_SUCCESS,
  FETCH_CHANGESETS,
  FETCH_CHANGESETS_FAILURE,
  FETCH_CHANGESETS_PENDING,
  FETCH_CHANGESETS_SUCCESS,
  fetchChangeset,
  fetchChangesetIfNeeded,
  fetchChangesets,
  fetchChangesetsSuccess,
  fetchChangesetSuccess,
  getChangeset,
  getChangesets,
  getFetchChangesetFailure,
  getFetchChangesetsFailure,
  isFetchChangesetPending,
  isFetchChangesetsPending,
  selectListAsCollection,
  shouldFetchChangeset
} from "./changesets";

const branch = {
  name: "specific",
  revision: "123",
  _links: {
    history: {
      href: "http://scm.hitchhicker.com/api/v2/repositories/foo/bar/branches/specific/changesets"
    }
  }
};

const repository = {
  namespace: "foo",
  name: "bar",
  type: "GIT",
  _links: {
    self: {
      href: "http://scm.hitchhicker.com/api/v2/repositories/foo/bar"
    },
    changesets: {
      href: "http://scm.hitchhicker.com/api/v2/repositories/foo/bar/changesets"
    },
    branches: {
      href: "http://scm.hitchhicker.com/api/v2/repositories/foo/bar/branches/specific/branches"
    }
  }
};

const changesets = {};

describe("changesets", () => {
  describe("fetching of changesets", () => {
    const DEFAULT_BRANCH_URL = "http://scm.hitchhicker.com/api/v2/repositories/foo/bar/changesets";
    const SPECIFIC_BRANCH_URL = "http://scm.hitchhicker.com/api/v2/repositories/foo/bar/branches/specific/changesets";

    const mockStore = configureMockStore([thunk]);

    afterEach(() => {
      fetchMock.reset();
      fetchMock.restore();
    });

    const changesetId = "aba876c0625d90a6aff1494f3d161aaa7008b958";

    it("should fetch changeset", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "/" + changesetId, "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESET_PENDING,
          itemId: "foo/bar/" + changesetId
        },
        {
          type: FETCH_CHANGESET_SUCCESS,
          payload: {
            changeset: {},
            id: changesetId,
            repository: repository
          },
          itemId: "foo/bar/" + changesetId
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangeset(repository, changesetId)).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should fail fetching changeset on error", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "/" + changesetId, 500);

      const expectedActions = [
        {
          type: FETCH_CHANGESET_PENDING,
          itemId: "foo/bar/" + changesetId
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangeset(repository, changesetId)).then(() => {
        expect(store.getActions()[0]).toEqual(expectedActions[0]);
        expect(store.getActions()[1].type).toEqual(FETCH_CHANGESET_FAILURE);
        expect(store.getActions()[1].payload).toBeDefined();
      });
    });

    it("should fetch changeset if needed", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "/id3", "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESET_PENDING,
          itemId: "foo/bar/id3"
        },
        {
          type: FETCH_CHANGESET_SUCCESS,
          payload: {
            changeset: {},
            id: "id3",
            repository: repository
          },
          itemId: "foo/bar/id3"
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesetIfNeeded(repository, "id3")).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should not fetch changeset if not needed", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL + "/id1", 500);

      const state = {
        changesets: {
          "foo/bar": {
            byId: {
              id1: {
                id: "id1"
              },
              id2: {
                id: "id2"
              }
            }
          }
        }
      };

      const store = mockStore(state);
      return expect(store.dispatch(fetchChangesetIfNeeded(repository, "id1"))).toEqual(undefined);
    });

    it("should fetch changesets for default branch", () => {
      fetchMock.getOnce(DEFAULT_BRANCH_URL, "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          itemId: "foo/bar"
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: {
            repository,
            undefined,
            changesets
          },
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
          itemId
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: {
            repository,
            branch,
            changesets
          },
          itemId
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesets(repository, branch)).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should fail fetching changesets on error", () => {
      const itemId = "foo/bar";
      fetchMock.getOnce(DEFAULT_BRANCH_URL, 500);

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
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
          itemId
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesets(repository, branch)).then(() => {
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
          itemId: "foo/bar"
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: {
            repository,
            undefined,
            changesets
          },
          itemId: "foo/bar"
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesets(repository, undefined, 5)).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should fetch changesets by branch and page", () => {
      fetchMock.getOnce(SPECIFIC_BRANCH_URL + "?page=4", "{}");

      const expectedActions = [
        {
          type: FETCH_CHANGESETS_PENDING,
          itemId: "foo/bar/specific"
        },
        {
          type: FETCH_CHANGESETS_SUCCESS,
          payload: {
            repository,
            branch,
            changesets
          },
          itemId: "foo/bar/specific"
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchChangesets(repository, branch, 5)).then(() => {
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
          {
            id: "changeset1",
            author: {
              mail: "z@phod.com",
              name: "zaphod"
            }
          },
          {
            id: "changeset2",
            description: "foo"
          },
          {
            id: "changeset3",
            description: "bar"
          }
        ],
        _embedded: {
          tags: [],
          branches: [],
          parents: []
        }
      }
    };

    it("should set state to received changesets", () => {
      const newState = reducer({}, fetchChangesetsSuccess(repository, undefined, responseBody));
      expect(newState).toBeDefined();
      expect(newState["foo/bar"].byId["changeset1"].author.mail).toEqual("z@phod.com");
      expect(newState["foo/bar"].byId["changeset2"].description).toEqual("foo");
      expect(newState["foo/bar"].byId["changeset3"].description).toEqual("bar");
      expect(newState["foo/bar"].byBranch[""]).toEqual({
        entry: {
          page: 1,
          pageTotal: 10,
          _links: {}
        },
        entries: ["changeset1", "changeset2", "changeset3"]
      });
    });

    it("should store the changeset list to branch", () => {
      const newState = reducer({}, fetchChangesetsSuccess(repository, branch, responseBody));

      expect(newState["foo/bar"].byId["changeset1"]).toBeDefined();
      expect(newState["foo/bar"].byBranch["specific"].entries).toEqual(["changeset1", "changeset2", "changeset3"]);
    });

    it("should not remove existing changesets", () => {
      const state = {
        "foo/bar": {
          byId: {
            id2: {
              id: "id2"
            },
            id1: {
              id: "id1"
            }
          },
          byBranch: {
            "": {
              entries: ["id1", "id2"]
            }
          }
        }
      };

      const newState = reducer(state, fetchChangesetsSuccess(repository, undefined, responseBody));

      const fooBar = newState["foo/bar"];

      expect(fooBar.byBranch[""].entries).toEqual(["changeset1", "changeset2", "changeset3"]);
      expect(fooBar.byId["id2"]).toEqual({
        id: "id2"
      });
      expect(fooBar.byId["id1"]).toEqual({
        id: "id1"
      });
    });

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
          "foo/bar": {
            byId: {
              id2: {
                id: "id2",
                author: {
                  mail: "mail@author.com",
                  name: "author"
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
          }
        },
        fetchChangesetSuccess(responseBodySingleChangeset, repository, "id3")
      );

      expect(newState).toBeDefined();
      expect(newState["foo/bar"].byId["id3"].description).toEqual("added testChangeset");
      expect(newState["foo/bar"].byId["id3"].author.mail).toEqual("z@phod.com");
      expect(newState["foo/bar"].byId["id2"]).toBeDefined();
      expect(newState["foo/bar"].byId["id3"]).toBeDefined();
      expect(newState["foo/bar"].list).toEqual({
        entry: {
          page: 1,
          pageTotal: 10,
          _links: {}
        },
        entries: ["id2"]
      });
    });
  });

  describe("changeset selectors", () => {
    const error = new Error("Something went wrong");

    it("should return changeset", () => {
      const state = {
        changesets: {
          "foo/bar": {
            byId: {
              id1: {
                id: "id1"
              },
              id2: {
                id: "id2"
              }
            }
          }
        }
      };
      const result = getChangeset(state, repository, "id1");
      expect(result).toEqual({
        id: "id1"
      });
    });

    it("should return null if changeset does not exist", () => {
      const state = {
        changesets: {
          "foo/bar": {
            byId: {
              id1: {
                id: "id1"
              },
              id2: {
                id: "id2"
              }
            }
          }
        }
      };
      const result = getChangeset(state, repository, "id3");
      expect(result).toEqual(null);
    });

    it("should return true if changeset does not exist", () => {
      const state = {
        changesets: {
          "foo/bar": {
            byId: {
              id1: {
                id: "id1"
              },
              id2: {
                id: "id2"
              }
            }
          }
        }
      };
      const result = shouldFetchChangeset(state, repository, "id3");
      expect(result).toEqual(true);
    });

    it("should return false if changeset exists", () => {
      const state = {
        changesets: {
          "foo/bar": {
            byId: {
              id1: {
                id: "id1"
              },
              id2: {
                id: "id2"
              }
            }
          }
        }
      };
      const result = shouldFetchChangeset(state, repository, "id2");
      expect(result).toEqual(false);
    });

    it("should return true, when fetching changeset is pending", () => {
      const state = {
        pending: {
          [FETCH_CHANGESET + "/foo/bar/id1"]: true
        }
      };

      expect(isFetchChangesetPending(state, repository, "id1")).toBeTruthy();
    });

    it("should return false, when fetching changeset is not pending", () => {
      expect(isFetchChangesetPending({}, repository, "id1")).toEqual(false);
    });

    it("should return error if fetching changeset failed", () => {
      const state = {
        failure: {
          [FETCH_CHANGESET + "/foo/bar/id1"]: error
        }
      };

      expect(getFetchChangesetFailure(state, repository, "id1")).toEqual(error);
    });

    it("should return false if fetching changeset did not fail", () => {
      expect(getFetchChangesetFailure({}, repository, "id1")).toBeUndefined();
    });

    it("should get all changesets for a given repository", () => {
      const state = {
        changesets: {
          "foo/bar": {
            byId: {
              id2: {
                id: "id2"
              },
              id1: {
                id: "id1"
              }
            },
            byBranch: {
              "": {
                entries: ["id1", "id2"]
              }
            }
          }
        }
      };
      const result = getChangesets(state, repository);
      expect(result).toEqual([
        {
          id: "id1"
        },
        {
          id: "id2"
        }
      ]);
    });

    it("should return always the same changeset array for the given parameters", () => {
      const state = {
        changesets: {
          "foo/bar": {
            byId: {
              id2: {
                id: "id2"
              },
              id1: {
                id: "id1"
              }
            },
            byBranch: {
              "": {
                entries: ["id1", "id2"]
              }
            }
          }
        }
      };
      const one = getChangesets(state, repository);
      const two = getChangesets(state, repository);
      expect(one).toBe(two);
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

    it("should return list as collection for the default branch", () => {
      const state = {
        changesets: {
          "foo/bar": {
            byId: {
              id2: {
                id: "id2"
              },
              id1: {
                id: "id1"
              }
            },
            byBranch: {
              "": {
                entry: {
                  page: 1,
                  pageTotal: 10,
                  _links: {}
                },
                entries: ["id1", "id2"]
              }
            }
          }
        }
      };

      const collection = selectListAsCollection(state, repository);
      expect(collection.page).toBe(1);
      expect(collection.pageTotal).toBe(10);
    });

    it("should return always the same empty object", () => {
      const state = {
        changesets: {}
      };
      const one = selectListAsCollection(state, repository);
      const two = selectListAsCollection(state, repository);
      expect(one).toBe(two);
    });
  });
});
