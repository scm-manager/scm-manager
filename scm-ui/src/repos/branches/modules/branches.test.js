import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  FETCH_BRANCHES,
  FETCH_BRANCHES_FAILURE,
  FETCH_BRANCHES_PENDING,
  FETCH_BRANCHES_SUCCESS,
  FETCH_BRANCH_PENDING,
  FETCH_BRANCH_SUCCESS,
  FETCH_BRANCH_FAILURE,
  CREATE_BRANCH,
  CREATE_BRANCH_FAILURE,
  CREATE_BRANCH_PENDING,
  CREATE_BRANCH_SUCCESS,
  fetchBranches,
  fetchBranch,
  fetchBranchSuccess,
  getBranch,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending,
  createBranch,
  isCreateBranchPending,
  getCreateBranchFailure,
  isPermittedToCreateBranches
} from "./branches";

const namespace = "foo";
const name = "bar";
const key = namespace + "/" + name;
const repository = {
  namespace: "foo",
  name: "bar",
  _links: {
    branches: {
      href: "http://scm/api/rest/v2/repositories/foo/bar/branches"
    }
  }
};

const branch1 = { name: "branch1", revision: "revision1" };
const branch2 = { name: "branch2", revision: "revision2" };
const branch3 = { name: "branch3", revision: "revision3" };
const branchRequest = { name: "newBranch", source: "master" };
const newBranch = { name: "newBranch", revision: "rev3" };

describe("branches", () => {
  describe("fetch branches", () => {
    const URL = "http://scm/api/rest/v2/repositories/foo/bar/branches";
    const mockStore = configureMockStore([thunk]);

    afterEach(() => {
      fetchMock.reset();
      fetchMock.restore();
    });

    it("should fetch branches", () => {
      const collection = {};

      fetchMock.getOnce(URL, "{}");

      const expectedActions = [
        {
          type: FETCH_BRANCHES_PENDING,
          payload: { repository },
          itemId: key
        },
        {
          type: FETCH_BRANCHES_SUCCESS,
          payload: { data: collection, repository },
          itemId: key
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchBranches(repository)).then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
    });

    it("should fail fetching branches on HTTP 500", () => {
      const collection = {};

      fetchMock.getOnce(URL, 500);

      const expectedActions = [
        {
          type: FETCH_BRANCHES_PENDING,
          payload: { repository },
          itemId: key
        },
        {
          type: FETCH_BRANCHES_FAILURE,
          payload: { error: collection, repository },
          itemId: key
        }
      ];

      const store = mockStore({});
      return store.dispatch(fetchBranches(repository)).then(() => {
        expect(store.getActions()[0]).toEqual(expectedActions[0]);
        expect(store.getActions()[1].type).toEqual(FETCH_BRANCHES_FAILURE);
      });
    });

    it("should successfully fetch single branch", () => {
      fetchMock.getOnce(URL + "/branch1", branch1);

      const store = mockStore({});
      return store.dispatch(fetchBranch(repository, "branch1")).then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(FETCH_BRANCH_PENDING);
        expect(actions[1].type).toEqual(FETCH_BRANCH_SUCCESS);
        expect(actions[1].payload).toBeDefined();
      });
    });

    it("should fail fetching single branch on HTTP 500", () => {
      fetchMock.getOnce(URL + "/branch2", {
        status: 500
      });

      const store = mockStore({});
      return store.dispatch(fetchBranch(repository, "branch2")).then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(FETCH_BRANCH_PENDING);
        expect(actions[1].type).toEqual(FETCH_BRANCH_FAILURE);
        expect(actions[1].payload).toBeDefined();
      });
    });

    it("should create a branch successfully", () => {
      //branchrequest answer
      fetchMock.postOnce(URL, {
        status: 201,
        headers: {
          location: URL + "/newBranch"
        }
      });

      //branch answer
      fetchMock.getOnce(URL + "/newBranch", newBranch);

      const store = mockStore({});
      return store
        .dispatch(createBranch(URL, repository, branchRequest))
        .then(() => {
          const actions = store.getActions();
          expect(actions[0].type).toEqual(CREATE_BRANCH_PENDING);
          expect(actions[1].type).toEqual(CREATE_BRANCH_SUCCESS);
        });
    });

    it("should call the callback with the branch from the location header", () => {
      //branchrequest answer
      fetchMock.postOnce(URL, {
        status: 201,
        headers: {
          location: URL + "/newBranch"
        }
      });

      //branch answer
      fetchMock.getOnce(URL + "/newBranch", newBranch);

      const store = mockStore({});

      let receivedBranch = null;

      const callback = branch => {
        receivedBranch = branch;
      };

      return store
        .dispatch(createBranch(URL, repository, branchRequest, callback))
        .then(() => {
          expect(receivedBranch).toEqual(newBranch);
        });
    });

    it("should fail creating a branch on HTTP 500", () => {
      fetchMock.postOnce(URL, {
        status: 500
      });

      const store = mockStore({});
      return store
        .dispatch(createBranch(URL, repository, branchRequest))
        .then(() => {
          const actions = store.getActions();
          expect(actions[0].type).toEqual(CREATE_BRANCH_PENDING);
          expect(actions[1].type).toEqual(CREATE_BRANCH_FAILURE);
        });
    });
  });

  describe("branches reducer", () => {
    const branches = {
      _embedded: {
        branches: [branch1, branch2]
      },
      _links: {
        self: {
          href: "/self"
        },
        create: {
          href: "/create"
        }
      }
    };
    const action = {
      type: FETCH_BRANCHES_SUCCESS,
      payload: {
        repository,
        data: branches
      }
    };

    it("should store the branches", () => {
      const newState = reducer({}, action);
      const repoState = newState["foo/bar"];

      expect(repoState.list._links.create.href).toEqual("/create");
      expect(repoState.list._embedded.branches).toEqual(["branch1", "branch2"]);

      expect(repoState.byName.branch1).toEqual(branch1);
      expect(repoState.byName.branch2).toEqual(branch2);
    });

    it("should store a single branch", () => {
      const newState = reducer({}, fetchBranchSuccess(repository, branch1));
      const repoState = newState["foo/bar"];

      expect(repoState.list).toBeUndefined();
      expect(repoState.byName.branch1).toEqual(branch1);
    });

    it("should add a single branch", () => {
      const state = {
        "foo/bar": {
          list: {
            _links: {},
            _embedded: {
              branches: ["branch1"]
            }
          },
          byName: {
            branch1: branch1
          }
        }
      };
      const newState = reducer(state, fetchBranchSuccess(repository, branch2));
      const repoState = newState["foo/bar"];
      const byName = repoState.byName;

      expect(repoState.list._embedded.branches).toEqual(["branch1"]);
      expect(byName.branch1).toEqual(branch1);
      expect(byName.branch2).toEqual(branch2);
    });

    it("should not overwrite non related repositories", () => {
      const state = {
        "scm/core": {
          byName: {
            branch1: branch1
          }
        }
      };
      const newState = reducer(state, fetchBranchSuccess(repository, branch1));
      const byName = newState["scm/core"].byName;

      expect(byName.branch1).toEqual(branch1);
    });

    it("should overwrite existing branch", () => {
      const state = {
        "foo/bar": {
          byName: {
            branch1: {
              name: "branch1",
              revision: "xyz"
            }
          }
        }
      };
      const newState = reducer(state, fetchBranchSuccess(repository, branch1));
      const byName = newState["foo/bar"].byName;

      expect(byName.branch1.revision).toEqual("revision1");
    });

    it("should not overwrite existing branches", () => {
      const state = {
        "foo/bar": {
          byName: {
            branch1,
            branch2,
            branch3
          }
        }
      };

      const newState = reducer(state, action);
      expect(newState["foo/bar"].byName.branch1).toEqual(branch1);
      expect(newState["foo/bar"].byName.branch2).toEqual(branch2);
      expect(newState["foo/bar"].byName.branch3).toEqual(branch3);
    });
  });

  describe("branch selectors", () => {
    const error = new Error("Something went wrong");

    const state = {
      branches: {
        "foo/bar": {
          list: {
            _links: {},
            _embedded: {
              branches: ["branch1", "branch2"]
            }
          },
          byName: {
            branch1: branch1,
            branch2: branch2
          }
        }
      }
    };

    it("should return true, when fetching branches is pending", () => {
      const state = {
        pending: {
          [FETCH_BRANCHES + "/foo/bar"]: true
        }
      };

      expect(isFetchBranchesPending(state, repository)).toBeTruthy();
    });

    it("should return branches", () => {
      const branches = getBranches(state, repository);
      expect(branches.length).toEqual(2);
      expect(branches).toContain(branch1);
      expect(branches).toContain(branch2);
    });

    it("should return always the same reference for branches", () => {
      const one = getBranches(state, repository);
      const two = getBranches(state, repository);
      expect(one).toBe(two);
    });

    it("should not return cached reference, if branches have changed", () => {
      const one = getBranches(state, repository);
      const newState = {
        branches: {
          "foo/bar": {
            list: {
              _links: {},
              _embedded: {
                branches: ["branch2", "branch3"]
              }
            },
            byName: {
              branch2,
              branch3
            }
          }
        }
      };
      const two = getBranches(newState, repository);
      expect(one).not.toBe(two);
      expect(two).not.toContain(branch1);
      expect(two).toContain(branch2);
      expect(two).toContain(branch3);
    });

    it("should return undefined, if no branches for the repository available", () => {
      const branches = getBranches({ branches: {} }, repository);
      expect(branches).toBeUndefined();
    });

    it("should return single branch by name", () => {
      const branch = getBranch(state, repository, "branch1");
      expect(branch).toEqual(branch1);
    });

    it("should return same reference for single branch by name", () => {
      const one = getBranch(state, repository, "branch1");
      const two = getBranch(state, repository, "branch1");
      expect(one).toBe(two);
    });

    it("should return undefined if branch does not exist", () => {
      const branch = getBranch(state, repository, "branch42");
      expect(branch).toBeUndefined();
    });

    it("should return true if the branches list contains the create link", () => {
      const stateWithLink = {
        branches: {
          "foo/bar": {
            ...state.branches["foo/bar"],
            list: {
              ...state.branches["foo/bar"].list,
              _links: {
                create: {
                  href: "http://create-it"
                }
              }
            }
          }
        }
      };

      const permitted = isPermittedToCreateBranches(stateWithLink, repository);
      expect(permitted).toBe(true);
    });

    it("should return false if the create link is missing", () => {
      const permitted = isPermittedToCreateBranches(state, repository);
      expect(permitted).toBe(false);
    });

    it("should return error if fetching branches failed", () => {
      const state = {
        failure: {
          [FETCH_BRANCHES + "/foo/bar"]: error
        }
      };

      expect(getFetchBranchesFailure(state, repository)).toEqual(error);
    });

    it("should return false if fetching branches did not fail", () => {
      expect(getFetchBranchesFailure({}, repository)).toBeUndefined();
    });

    it("should return true if create branch is pending", () => {
      const state = {
        pending: {
          [CREATE_BRANCH + "/foo/bar"]: true
        }
      };
      expect(isCreateBranchPending(state, repository)).toBe(true);
    });

    it("should return false if create branch is not pending", () => {
      const state = {
        pending: {
          [CREATE_BRANCH + "/foo/bar"]: false
        }
      };
      expect(isCreateBranchPending(state, repository)).toBe(false);
    });

    it("should return error when create branch did fail", () => {
      const state = {
        failure: {
          [CREATE_BRANCH]: error
        }
      };
      expect(getCreateBranchFailure(state)).toEqual(error);
    });

    it("should return undefined when create branch did not fail", () => {
      expect(getCreateBranchFailure({})).toBe(undefined);
    });
  });
});
