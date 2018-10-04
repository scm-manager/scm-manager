import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  FETCH_BRANCHES_FAILURE,
  FETCH_BRANCHES_PENDING,
  FETCH_BRANCHES_SUCCESS,
  fetchBranches,
  getBranchNames
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
      expect(newState[key].byNames["branch1"]).toEqual(branch1);
      expect(newState[key].byNames["branch2"]).toEqual(branch2);
    });

    it("should not delete existing branches from state", () => {
      const oldState = {
        "foo/bar": {
          byNames: {
            branch3: branch3
          }
        }
      };

      const newState = reducer(oldState, action);
      expect(newState[key].byNames["branch1"]).toEqual(branch1);
      expect(newState[key].byNames["branch2"]).toEqual(branch2);
      expect(newState[key].byNames["branch3"]).toEqual(branch3);
    });
  });

  describe("branch selectors", () => {
    it("should return branches names", () => {
      const state = {
        branches: {
          [key]: {
            byNames: {
              branch1: branch1,
              branch2: branch2
            }
          }
        }
      };
      const names = getBranchNames(state, repository);
      expect(names.length).toEqual(2);
      expect(names).toContain("branch1");
      expect(names).toContain("branch2");
    });

    it("should return branches", () => {
      const state = {
        branches: {
          [key]: {}
        }
      };
    });
  });
});
