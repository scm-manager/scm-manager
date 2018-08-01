//@flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import reducer, {
  fetchGroups,
  FETCH_GROUPS,
  FETCH_GROUPS_PENDING,
  FETCH_GROUPS_SUCCESS,
  FETCH_GROUPS_FAILURE,
  fetchGroupsSuccess,
  isPermittedToCreateGroups,
  getGroupsFromState,
  getFetchGroupsFailure,
  isFetchGroupsPending,
  selectListAsCollection,
  fetchGroup,
  FETCH_GROUP_PENDING,
  FETCH_GROUP_SUCCESS,
  FETCH_GROUP_FAILURE,
  fetchGroupSuccess,
  getFetchGroupFailure,
  FETCH_GROUP,
  isFetchGroupPending,
  getGroupByName,
  createGroup,
  CREATE_GROUP_SUCCESS,
  CREATE_GROUP_PENDING,
  CREATE_GROUP_FAILURE,
  isCreateGroupPending,
  CREATE_GROUP,
  getCreateGroupFailure,
  deleteGroup,
  DELETE_GROUP_PENDING,
  DELETE_GROUP_SUCCESS,
  DELETE_GROUP_FAILURE,
  DELETE_GROUP,
  deleteGroupSuccess,
  isDeleteGroupPending,
  getDeleteGroupFailure
} from "./groups";
const GROUPS_URL = "/scm/api/rest/v2/groups";

const error = new Error("You have an error!");

const humanGroup = {
  creationDate: "2018-07-31T08:39:07.860Z",
  description: "This is a group",
  name: "humanGroup",
  type: "xml",
  properties: {},
  members: ["userZaphod"],
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/rest/v2/groups/humanGroup"
    },
    delete: {
      href: "http://localhost:8081/scm/api/rest/v2/groups/humanGroup"
    },
    update: {
      href:"http://localhost:8081/scm/api/rest/v2/groups/humanGroup"
    }
  },
  _embedded: {
    members: [
      {
        name: "userZaphod",
        _links: {
          self: {
            href: "http://localhost:8081/scm/api/rest/v2/users/userZaphod"
          }
        }
      }
    ]
  }
};

const emptyGroup = {
  creationDate: "2018-07-31T08:39:07.860Z",
  description: "This is a group",
  name: "emptyGroup",
  type: "xml",
  properties: {},
  members: [],
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/rest/v2/groups/emptyGroup"
    },
    delete: {
      href: "http://localhost:8081/scm/api/rest/v2/groups/emptyGroup"
    },
    update: {
      href:"http://localhost:8081/scm/api/rest/v2/groups/emptyGroup"
    }
  },
  _embedded: {
    members: []
  }
};

const responseBody = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/?page=0&pageSize=10"
    },
    first: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/?page=0&pageSize=10"
    },
    last: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/"
    }
  },
  _embedded: {
    groups: [humanGroup, emptyGroup]
  }
};

const response = {
  headers: { "content-type": "application/json" },
  responseBody
};

describe("groups fetch()", () => {
  const mockStore = configureMockStore([thunk]);
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch groups", () => {
    fetchMock.getOnce(GROUPS_URL, response);

    const expectedActions = [
      { type: FETCH_GROUPS_PENDING },
      {
        type: FETCH_GROUPS_SUCCESS,
        payload: response
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchGroups()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fail getting groups on HTTP 500", () => {
    fetchMock.getOnce(GROUPS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchGroups()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_GROUPS_PENDING);
      expect(actions[1].type).toEqual(FETCH_GROUPS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should sucessfully fetch single group", () => {
    fetchMock.getOnce(GROUPS_URL + "/humanGroup", humanGroup);

    const store = mockStore({});
    return store.dispatch(fetchGroup("humanGroup")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_GROUP_PENDING);
      expect(actions[1].type).toEqual(FETCH_GROUP_SUCCESS);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should fail fetching single group on HTTP 500", () => {
    fetchMock.getOnce(GROUPS_URL + "/humanGroup", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchGroup("humanGroup")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_GROUP_PENDING);
      expect(actions[1].type).toEqual(FETCH_GROUP_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });


  it("should successfully create group", () => {
    fetchMock.postOnce(GROUPS_URL, {
      status: 201
    });

    const store = mockStore({});
    return store.dispatch(createGroup(humanGroup)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(CREATE_GROUP_PENDING);
      expect(actions[1].type).toEqual(CREATE_GROUP_SUCCESS);
    });
  });

  it("should call the callback after creating group", () => {
    fetchMock.postOnce(GROUPS_URL, {
      status: 201
    });
    let called = false;

    const callMe = () => {
      called = true;
    }
    const store = mockStore({});
    return store.dispatch(createGroup(humanGroup, callMe)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(CREATE_GROUP_PENDING);
      expect(actions[1].type).toEqual(CREATE_GROUP_SUCCESS);
      expect(called).toEqual(true);
    });
  });
  

  it("should fail creating group on HTTP 500", () => {
    fetchMock.postOnce(GROUPS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(createGroup(humanGroup)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(CREATE_GROUP_PENDING);
      expect(actions[1].type).toEqual(CREATE_GROUP_FAILURE);
      expect(actions[1].payload).toBeDefined();
      expect(actions[1].payload instanceof Error).toBeTruthy();
    });
  });

  it("should delete successfully group humanGroup", () => {
    fetchMock.deleteOnce("http://localhost:8081/scm/api/rest/v2/groups/humanGroup", {
      status: 204
    });

    const store = mockStore({});
    return store.dispatch(deleteGroup(humanGroup)).then(() => {
      const actions = store.getActions();
      expect(actions.length).toBe(2);
      expect(actions[0].type).toEqual(DELETE_GROUP_PENDING);
      expect(actions[0].payload).toBe(humanGroup);
      expect(actions[1].type).toEqual(DELETE_GROUP_SUCCESS);
    });
  });

  it("should call the callback, after successful delete", () => {
    fetchMock.deleteOnce("http://localhost:8081/scm/api/rest/v2/groups/humanGroup", {
      status: 204
    });

    let called = false;
    const callMe = () => {
      called = true;
    };

    const store = mockStore({});
    return store.dispatch(deleteGroup(humanGroup, callMe)).then(() => {
      expect(called).toBeTruthy();
    });
  });

  it("should fail to delete group humanGroup", () => {
    fetchMock.deleteOnce("http://localhost:8081/scm/api/rest/v2/groups/humanGroup", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(deleteGroup(humanGroup)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(DELETE_GROUP_PENDING);
      expect(actions[0].payload).toBe(humanGroup);
      expect(actions[1].type).toEqual(DELETE_GROUP_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

});

describe("groups reducer", () => {

  it("should update state correctly according to FETCH_GROUPS_SUCCESS action", () => {

    const newState = reducer({}, fetchGroupsSuccess(responseBody));

    expect(newState.list).toEqual({
      entries: ["humanGroup", "emptyGroup"],
      entry: {
        groupCreatePermission: true,
        page: 0,
        pageTotal: 1,
        _links: responseBody._links
      }
    });

    expect(newState.byNames).toEqual({
      humanGroup: humanGroup,
      emptyGroup: emptyGroup
    });

    expect(newState.list.entry.groupCreatePermission).toBeTruthy();
  });

  it("should set groupCreatePermission to true if update link is present", () => {
    const newState = reducer({}, fetchGroupsSuccess(responseBody));

    expect(newState.list.entry.groupCreatePermission).toBeTruthy();
  });

  it("should not replace whole byNames map when fetching groups", () => {
    const oldState = {
      byNames: {
        emptyGroup: emptyGroup
      }
    };

    const newState = reducer(oldState, fetchGroupsSuccess(responseBody));
    expect(newState.byNames["humanGroup"]).toBeDefined();
    expect(newState.byNames["emptyGroup"]).toBeDefined();
  });

  it("should set groupCreatePermission to true if create link is present", () => {
    const newState = reducer({}, fetchGroupsSuccess(responseBody));

    expect(newState.list.entry.groupCreatePermission).toBeTruthy();
    expect(newState.list.entries).toEqual(["humanGroup", "emptyGroup"]);
    expect(newState.byNames["emptyGroup"]).toBeTruthy();
    expect(newState.byNames["humanGroup"]).toBeTruthy();
  });


  it("should update state according to FETCH_GROUP_SUCCESS action", () => {
    const newState = reducer({}, fetchGroupSuccess(emptyGroup));
    expect(newState.byNames["emptyGroup"]).toBe(emptyGroup);
  });

  it("should affect groups state nor the state of other groups", () => {
    const newState = reducer(
      {
        list: {
          entries: ["humanGroup"]
        }
      },
      fetchGroupSuccess(emptyGroup)
    );
    expect(newState.byNames["emptyGroup"]).toBe(emptyGroup);
    expect(newState.list.entries).toEqual(["humanGroup"]);
  });

  it("should remove group from state when delete succeeds", () => {
    const state = {
      list: {
        entries: ["humanGroup", "emptyGroup"]
      },
      byNames: {
        humanGroup: humanGroup,
        emptyGroup: emptyGroup
      }
    };

    const newState = reducer(state, deleteGroupSuccess(emptyGroup));
    expect(newState.byNames["humanGroup"]).toBeDefined();
    expect(newState.byNames["emptyGroup"]).toBeFalsy();
    expect(newState.list.entries).toEqual(["humanGroup"]);
  });


});

describe("selector tests", () => {
  it("should return an empty object", () => {
    expect(selectListAsCollection({})).toEqual({});
    expect(selectListAsCollection({ groups: { a: "a" } })).toEqual({});
  });

  it("should return a state slice collection", () => {
    const collection = {
      page: 3,
      totalPages: 42
    };

    const state = {
      groups: {
        list: {
          entry: collection
        }
      }
    };
    expect(selectListAsCollection(state)).toBe(collection);
  });

  it("should return false", () => {
    expect(isPermittedToCreateGroups({})).toBe(false);
    expect(isPermittedToCreateGroups({ groups: { list: { entry: {} } } })).toBe(
      false
    );
    expect(
      isPermittedToCreateGroups({
        groups: { list: { entry: { groupCreatePermission: false } } }
      })
    ).toBe(false);
  });

  it("should return true", () => {
    const state = {
      groups: {
        list: {
          entry: {
            groupCreatePermission: true
          }
        }
      }
    };
    expect(isPermittedToCreateGroups(state)).toBe(true);
  });

  it("should get groups from state", () => {
    const state = {
      groups: {
        list: {
          entries: ["a", "b"]
        },
        byNames: {
          a: { name: "a" },
          b: { name: "b" }
        }
      }
    };
    expect(getGroupsFromState(state)).toEqual([{ name: "a" }, { name: "b" }]);
  });

  it("should return true, when fetch groups is pending", () => {
    const state = {
      pending: {
        [FETCH_GROUPS]: true
      }
    };
    expect(isFetchGroupsPending(state)).toEqual(true);
  });

  it("should return false, when fetch groups is not pending", () => {
    expect(isFetchGroupsPending({})).toEqual(false);
  });

  it("should return error when fetch groups did fail", () => {
    const state = {
      failure: {
        [FETCH_GROUPS]: error
      }
    };
    expect(getFetchGroupsFailure(state)).toEqual(error);
  });

  it("should return undefined when fetch groups did not fail", () => {
    expect(getFetchGroupsFailure({})).toBe(undefined);
  });

  it("should return group emptyGroup", () => {
    const state = {
      groups: {
        byNames: {
          emptyGroup: emptyGroup
        }
      }
    };
    expect(getGroupByName(state, "emptyGroup")).toEqual(emptyGroup);
  });

  it("should return true, when fetch group humanGroup is pending", () => {
    const state = {
      pending: {
        [FETCH_GROUP + "/humanGroup"]: true
      }
    };
    expect(isFetchGroupPending(state, "humanGroup")).toEqual(true);
  });

  it("should return false, when fetch group humanGroup is not pending", () => {
    expect(isFetchGroupPending({}, "humanGroup")).toEqual(false);
  });

  it("should return error when fetch group humanGroup did fail", () => {
    const state = {
      failure: {
        [FETCH_GROUP + "/humanGroup"]: error
      }
    };
    expect(getFetchGroupFailure(state, "humanGroup")).toEqual(error);
  });

  it("should return undefined when fetch group humanGroup did not fail", () => {
    expect(getFetchGroupFailure({}, "humanGroup")).toBe(undefined);
  });

  it("should return true if create group is pending", () => {
    expect(isCreateGroupPending({pending: {
      [CREATE_GROUP]: true
    }})).toBeTruthy();
  })

  it("should return false if create group is not pending", () => {
    expect(isCreateGroupPending({})).toBe(false);
  })

  it("should return error if creating group failed", () => {
    expect(getCreateGroupFailure({
      failure: {
        [CREATE_GROUP]: error
      }
    })).toEqual(error)
  })

  it("should return undefined if creating group did not fail", () => {
    expect(getCreateGroupFailure({})).toBeUndefined()
  })


  it("should return true, when delete group humanGroup is pending", () => {
    const state = {
      pending: {
        [DELETE_GROUP + "/humanGroup"]: true
      }
    };
    expect(isDeleteGroupPending(state, "humanGroup")).toEqual(true);
  });

  it("should return false, when delete group humanGroup is not pending", () => {
    expect(isDeleteGroupPending({}, "humanGroup")).toEqual(false);
  });

  it("should return error when delete group humanGroup did fail", () => {
    const state = {
      failure: {
        [DELETE_GROUP + "/humanGroup"]: error
      }
    };
    expect(getDeleteGroupFailure(state, "humanGroup")).toEqual(error);
  });

  it("should return undefined when delete group humanGroup did not fail", () => {
    expect(getDeleteGroupFailure({}, "humanGroup")).toBe(undefined);
  });

});
