import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  FETCH_BRANCHES,
  FETCH_BRANCHES_FAILURE,
  FETCH_BRANCHES_PENDING,
  FETCH_BRANCHES_SUCCESS,
  FETCH_BRANCH,
  FETCH_BRANCH_PENDING,
  FETCH_BRANCH_SUCCESS,
  FETCH_BRANCH_FAILURE,
  fetchBranches,
  fetchBranchByName,
  fetchBranchSuccess,
  fetchBranch,
  getBranch,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
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
      return store.dispatch(fetchBranchByName(URL, "branch1")).then(() => {
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
      return store.dispatch(fetchBranchByName(URL, "branch2")).then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(FETCH_BRANCH_PENDING);
        expect(actions[1].type).toEqual(FETCH_BRANCH_FAILURE);
        expect(actions[1].payload).toBeDefined();
      });
    });
  });

  describe("branches reducer", () => {
    const branches = {
      _embedded: {
        branches: [branch1, branch2]
      }
    };
    const action = {
      type: FETCH_BRANCHES_SUCCESS,
      payload: {
        repository,
        data: branches
      }
    };

    it("should update state according to successful fetch", () => {
      const newState = reducer({}, action);
      expect(newState).toBeDefined();
      expect(newState[key]).toBeDefined();
      expect(newState[key]).toContain(branch1);
      expect(newState[key]).toContain(branch2);
    });

    it("should not delete existing branches from state", () => {
      const oldState = {
        "hitchhiker/heartOfGold": [branch3]
      };

      const newState = reducer(oldState, action);
      expect(newState[key]).toContain(branch1);
      expect(newState[key]).toContain(branch2);

      expect(newState["hitchhiker/heartOfGold"]).toContain(branch3);
    });

    it("should update state according to FETCH_BRANCH_SUCCESS action", () => {
      const newState = reducer({}, fetchBranchSuccess(branch3));
      expect(newState["branch3"]).toBe(branch3);
    });
  });

  describe("branch selectors", () => {
    const error = new Error("Something went wrong");

    const state = {
      branches: {
        [key]: [branch1, branch2]
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

    it("should return null, if no branches for the repository available", () => {
      const branches = getBranches({ branches: {} }, repository);
      expect(branches).toBeNull();
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
  });
});
