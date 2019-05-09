// @flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import reducer, {
  FETCH_ROLES,
  FETCH_ROLES_PENDING,
  FETCH_ROLES_SUCCESS,
  FETCH_ROLES_FAILURE,
  FETCH_ROLE,
  FETCH_ROLE_PENDING,
  FETCH_ROLE_SUCCESS,
  FETCH_ROLE_FAILURE,
  CREATE_ROLE,
  CREATE_ROLE_PENDING,
  CREATE_ROLE_SUCCESS,
  CREATE_ROLE_FAILURE,
  fetchRoles,
  getFetchRolesFailure,
  getRolesFromState,
  isFetchRolesPending,
  fetchRolesSuccess,
  fetchRoleByLink,
  fetchRoleByName,
  fetchRoleSuccess,
  isFetchRolePending,
  getFetchRoleFailure,
  createRole,
  isCreateRolePending,
  getCreateRoleFailure,
  getRoleByName,
  selectListAsCollection,
  isPermittedToCreateRoles
} from "./roles";

const role1 = {
  name: "specialrole",
  verbs: ["read", "pull", "push", "readPullRequest"],
  system: false,
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/repositoryRoles/specialrole"
    },
    delete: {
      href: "http://localhost:8081/scm/api/v2/repositoryRoles/specialrole"
    },
    update: {
      href: "http://localhost:8081/scm/api/v2/repositoryRoles/specialrole"
    }
  }
};
const role2 = {
  name: "WRITE",
  verbs: [
    "read",
    "pull",
    "push",
    "createPullRequest",
    "readPullRequest",
    "commentPullRequest",
    "mergePullRequest"
  ],
  system: true,
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/v2/repositoryRoles/WRITE"
    }
  }
};

const responseBody = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/v2/repositoryRoles/?page=0&pageSize=10"
    },
    first: {
      href:
        "http://localhost:8081/scm/api/v2/repositoryRoles/?page=0&pageSize=10"
    },
    last: {
      href:
        "http://localhost:8081/scm/api/v2/repositoryRoles/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:8081/scm/api/v2/repositoryRoles/"
    }
  },
  _embedded: {
    repositoryRoles: [role1, role2]
  }
};

const response = {
  headers: { "content-type": "application/json" },
  responseBody
};

const URL = "repositoryRoles";
const ROLES_URL = "/api/v2/repositoryRoles";
const ROLE1_URL = "http://localhost:8081/scm/api/v2/repositoryRoles/specialrole";

const error = new Error("FEHLER!");

describe("repository roles fetch", () => {
  const mockStore = configureMockStore([thunk]);
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch repository roles", () => {
    fetchMock.getOnce(ROLES_URL, response);

    const expectedActions = [
      { type: FETCH_ROLES_PENDING },
      {
        type: FETCH_ROLES_SUCCESS,
        payload: response
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchRoles(URL)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fail getting repository roles on HTTP 500", () => {
    fetchMock.getOnce(ROLES_URL, {
      status: 500
    });

    const store = mockStore({});

    return store.dispatch(fetchRoles(URL)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_ROLES_PENDING);
      expect(actions[1].type).toEqual(FETCH_ROLES_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should sucessfully fetch single role by name", () => {
    fetchMock.getOnce(ROLES_URL + "/specialrole", role1);

    const store = mockStore({});
    return store.dispatch(fetchRoleByName(URL, "specialrole")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_ROLE_PENDING);
      expect(actions[1].type).toEqual(FETCH_ROLE_SUCCESS);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should fail fetching single role by name on HTTP 500", () => {
    fetchMock.getOnce(ROLES_URL + "/specialrole", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchRoleByName(URL, "specialrole")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_ROLE_PENDING);
      expect(actions[1].type).toEqual(FETCH_ROLE_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should sucessfully fetch single role", () => {
    fetchMock.getOnce(ROLE1_URL, role1);

    const store = mockStore({});
    return store.dispatch(fetchRoleByLink(role1)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_ROLE_PENDING);
      expect(actions[1].type).toEqual(FETCH_ROLE_SUCCESS);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should fail fetching single role on HTTP 500", () => {
    fetchMock.getOnce(ROLE1_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchRoleByLink(role1)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_ROLE_PENDING);
      expect(actions[1].type).toEqual(FETCH_ROLE_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should add a role successfully", () => {
    // unmatched
    fetchMock.postOnce(ROLES_URL, {
      status: 204
    });

    // after create, the roles are fetched again
    fetchMock.getOnce(ROLES_URL, response);

    const store = mockStore({});

    return store.dispatch(createRole(URL, role1)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(CREATE_ROLE_PENDING);
      expect(actions[1].type).toEqual(CREATE_ROLE_SUCCESS);
    });
  });

  it("should fail adding a role on HTTP 500", () => {
    fetchMock.postOnce(ROLES_URL, {
      status: 500
    });

    const store = mockStore({});

    return store.dispatch(createRole(URL, role1)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(CREATE_ROLE_PENDING);
      expect(actions[1].type).toEqual(CREATE_ROLE_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should call the callback after role successfully created", () => {
    // unmatched
    fetchMock.postOnce(ROLES_URL, {
      status: 204
    });

    let callMe = "not yet";

    const callback = () => {
      callMe = "yeah";
    };

    const store = mockStore({});
    return store.dispatch(createRole(URL, role1, callback)).then(() => {
      expect(callMe).toBe("yeah");
    });
  });
});

describe("repository roles reducer", () => {
  it("should update state correctly according to FETCH_ROLES_SUCCESS action", () => {
    const newState = reducer({}, fetchRolesSuccess(responseBody));

    expect(newState.list).toEqual({
      entries: ["specialrole", "WRITE"],
      entry: {
        roleCreatePermission: true,
        page: 0,
        pageTotal: 1,
        _links: responseBody._links
      }
    });

    expect(newState.byNames).toEqual({
      specialrole: role1,
      WRITE: role2
    });

    expect(newState.list.entry.roleCreatePermission).toBeTruthy();
  });

  it("should set roleCreatePermission to true if update link is present", () => {
    const newState = reducer({}, fetchRolesSuccess(responseBody));

    expect(newState.list.entry.roleCreatePermission).toBeTruthy();
  });

  it("should not replace whole byNames map when fetching roles", () => {
    const oldState = {
      byNames: {
        WRITE: role2
      }
    };

    const newState = reducer(oldState, fetchRolesSuccess(responseBody));
    expect(newState.byNames["specialrole"]).toBeDefined();
    expect(newState.byNames["WRITE"]).toBeDefined();
  });

  it("should set roleCreatePermission to true if create link is present", () => {
    const newState = reducer({}, fetchRolesSuccess(responseBody));

    expect(newState.list.entry.roleCreatePermission).toBeTruthy();
    expect(newState.list.entries).toEqual(["specialrole", "WRITE"]);
    expect(newState.byNames["WRITE"]).toBeTruthy();
    expect(newState.byNames["specialrole"]).toBeTruthy();
  });

  it("should update state according to FETCH_ROLE_SUCCESS action", () => {
    const newState = reducer({}, fetchRoleSuccess(role2));
    expect(newState.byNames["WRITE"]).toBe(role2);
  });

  it("should affect roles state nor the state of other roles", () => {
    const newState = reducer(
      {
        list: {
          entries: ["specialrole"]
        }
      },
      fetchRoleSuccess(role2)
    );
    expect(newState.byNames["WRITE"]).toBe(role2);
    expect(newState.list.entries).toEqual(["specialrole"]);
  });
});

describe("repository roles selector", () => {
  it("should return an empty object", () => {
    expect(selectListAsCollection({})).toEqual({});
    expect(selectListAsCollection({ repositoryRoles: { a: "a" } })).toEqual({});
  });

  it("should return a state slice collection", () => {
    const collection = {
      page: 3,
      totalPages: 42
    };

    const state = {
      repositoryRoles: {
        list: {
          entry: collection
        }
      }
    };
    expect(selectListAsCollection(state)).toBe(collection);
  });

  it("should return false", () => {
    expect(isPermittedToCreateRoles({})).toBe(false);
    expect(
      isPermittedToCreateRoles({ repositoryRoles: { list: { entry: {} } } })
    ).toBe(false);
    expect(
      isPermittedToCreateRoles({
        repositoryRoles: { list: { entry: { roleCreatePermission: false } } }
      })
    ).toBe(false);
  });

  it("should return true", () => {
    const state = {
      repositoryRoles: {
        list: {
          entry: {
            roleCreatePermission: true
          }
        }
      }
    };
    expect(isPermittedToCreateRoles(state)).toBe(true);
  });

  it("should get repositoryRoles from state", () => {
    const state = {
      repositoryRoles: {
        list: {
          entries: ["a", "b"]
        },
        byNames: {
          a: { name: "a" },
          b: { name: "b" }
        }
      }
    };
    expect(getRolesFromState(state)).toEqual([{ name: "a" }, { name: "b" }]);
  });

  it("should return true, when fetch repositoryRoles is pending", () => {
    const state = {
      pending: {
        [FETCH_ROLES]: true
      }
    };
    expect(isFetchRolesPending(state)).toEqual(true);
  });

  it("should return false, when fetch repositoryRoles is not pending", () => {
    expect(isFetchRolesPending({})).toEqual(false);
  });

  it("should return error when fetch repositoryRoles did fail", () => {
    const state = {
      failure: {
        [FETCH_ROLES]: error
      }
    };
    expect(getFetchRolesFailure(state)).toEqual(error);
  });

  it("should return undefined when fetch repositoryRoles did not fail", () => {
    expect(getFetchRolesFailure({})).toBe(undefined);
  });

  it("should return true if create role is pending", () => {
    const state = {
      pending: {
        [CREATE_ROLE]: true
      }
    };
    expect(isCreateRolePending(state)).toBe(true);
  });

  it("should return false if create role is not pending", () => {
    const state = {
      pending: {
        [CREATE_ROLE]: false
      }
    };
    expect(isCreateRolePending(state)).toBe(false);
  });

  it("should return error when create role did fail", () => {
    const state = {
      failure: {
        [CREATE_ROLE]: error
      }
    };
    expect(getCreateRoleFailure(state)).toEqual(error);
  });

  it("should return undefined when create role did not fail", () => {
    expect(getCreateRoleFailure({})).toBe(undefined);
  });

  it("should return role1", () => {
    const state = {
      repositoryRoles: {
        byNames: {
          role1: role1
        }
      }
    };
    expect(getRoleByName(state, "role1")).toEqual(role1);
  });

  it("should return true, when fetch role2 is pending", () => {
    const state = {
      pending: {
        [FETCH_ROLE + "/role2"]: true
      }
    };
    expect(isFetchRolePending(state, "role2")).toEqual(true);
  });

  it("should return false, when fetch role2 is not pending", () => {
    expect(isFetchRolePending({}, "role2")).toEqual(false);
  });

  it("should return error when fetch role2 did fail", () => {
    const state = {
      failure: {
        [FETCH_ROLE + "/role2"]: error
      }
    };
    expect(getFetchRoleFailure(state, "role2")).toEqual(error);
  });

  it("should return undefined when fetch role2 did not fail", () => {
    expect(getFetchRoleFailure({}, "role2")).toBe(undefined);
  });
});
