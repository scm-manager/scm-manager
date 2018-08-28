// @flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  fetchPermissions,
  fetchPermissionsSuccess,
  getPermissionsOfRepo,
  isFetchPermissionsPending,
  getFetchPermissionsFailure,
  modifyPermission,
  modifyPermissionSuccess,
  MODIFY_PERMISSION_FAILURE,
  MODIFY_PERMISSION_PENDING,
  FETCH_PERMISSIONS,
  FETCH_PERMISSIONS_PENDING,
  FETCH_PERMISSIONS_SUCCESS,
  FETCH_PERMISSIONS_FAILURE,
  MODIFY_PERMISSION_SUCCESS
} from "./permissions";
import type { Permission, PermissionCollection } from "../types/Permissions";
import { modifyRepoSuccess } from "../../repos/modules/repos";

const hitchhiker_puzzle42Permission_user_eins: Permission = {
  name: "user_eins",
  type: "READ",
  groupPermission: true,
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/permissions/user_eins"
    },
    delete: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/permissions/user_eins"
    },
    update: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/permissions/user_eins"
    }
  }
};

const hitchhiker_puzzle42Permission_user_zwei: Permission = {
  name: "user_zwei",
  type: "WRITE",
  groupPermission: true,
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/permissions/user_zwei"
    },
    delete: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/permissions/user_zwei"
    },
    update: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/permissions/user_zwei"
    }
  }
};

const hitchhiker_puzzle42Permissions: PermissionCollection = [
  hitchhiker_puzzle42Permission_user_eins,
  hitchhiker_puzzle42Permission_user_zwei
];

const hitchhiker_puzzle42RepoPermissions = {
  _embedded: {
    permissions: hitchhiker_puzzle42Permissions
  }
};

describe("permission fetch", () => {
  const REPOS_URL = "/scm/api/rest/v2/repositories";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch permissions to repo hitchhiker/puzzle42", () => {
    fetchMock.getOnce(
      REPOS_URL + "/hitchhiker/puzzle42/permissions",
      hitchhiker_puzzle42RepoPermissions
    );

    const expectedActions = [
      {
        type: FETCH_PERMISSIONS_PENDING,
        payload: {
          namespace: "hitchhiker",
          name: "puzzle42"
        },
        itemId: "hitchhiker/puzzle42"
      },
      {
        type: FETCH_PERMISSIONS_SUCCESS,
        payload: hitchhiker_puzzle42RepoPermissions,
        itemId: "hitchhiker/puzzle42"
      }
    ];

    const store = mockStore({});
    return store
      .dispatch(fetchPermissions("hitchhiker", "puzzle42"))
      .then(() => {
        expect(store.getActions()).toEqual(expectedActions);
      });
  });

  it("should dispatch FETCH_PERMISSIONS_FAILURE, it the request fails", () => {
    fetchMock.getOnce(REPOS_URL + "/hitchhiker/puzzle42/permissions", {
      status: 500
    });

    const store = mockStore({});
    return store
      .dispatch(fetchPermissions("hitchhiker", "puzzle42"))
      .then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(FETCH_PERMISSIONS_PENDING);
        expect(actions[1].type).toEqual(FETCH_PERMISSIONS_FAILURE);
        expect(actions[1].payload).toBeDefined();
      });
  });

  it("should successfully modify user_eins permission", () => {
    fetchMock.putOnce(
      hitchhiker_puzzle42Permission_user_eins._links.update.href,
      {
        status: 204
      }
    );

    let editedPermission = { ...hitchhiker_puzzle42Permission_user_eins };
    editedPermission.type = "OWNER";

    const store = mockStore({});

    return store
      .dispatch(modifyPermission(editedPermission, "hitchhiker", "puzzle42"))
      .then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(MODIFY_PERMISSION_PENDING);
        expect(actions[1].type).toEqual(MODIFY_PERMISSION_SUCCESS);
      });
  });

  it("should successfully modify user_eins permission and call the callback", () => {
    fetchMock.putOnce(
      hitchhiker_puzzle42Permission_user_eins._links.update.href,
      {
        status: 204
      }
    );

    let editedPermission = { ...hitchhiker_puzzle42Permission_user_eins };
    editedPermission.type = "OWNER";

    const store = mockStore({});

    let called = false;
    const callback = () => {
      called = true;
    };

    return store
      .dispatch(
        modifyPermission(editedPermission, "hitchhiker", "puzzle42", callback)
      )
      .then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(MODIFY_PERMISSION_PENDING);
        expect(actions[1].type).toEqual(MODIFY_PERMISSION_SUCCESS);
        expect(called).toBe(true);
      });
  });

  it("should fail modifying on HTTP 500", () => {
    fetchMock.putOnce(
      hitchhiker_puzzle42Permission_user_eins._links.update.href,
      {
        status: 500
      }
    );

    let editedPermission = { ...hitchhiker_puzzle42Permission_user_eins };
    editedPermission.type = "OWNER";

    const store = mockStore({});

    return store
      .dispatch(modifyPermission(editedPermission, "hitchhiker", "puzzle42"))
      .then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(MODIFY_PERMISSION_PENDING);
        expect(actions[1].type).toEqual(MODIFY_PERMISSION_FAILURE);
        expect(actions[1].payload).toBeDefined();
      });
  });
});

describe("permissions reducer", () => {
  it("should return empty object, if state and action is undefined", () => {
    expect(reducer()).toEqual({});
  });

  it("should return the same state, if the action is undefined", () => {
    const state = { x: true };
    expect(reducer(state)).toBe(state);
  });

  it("should return the same state, if the action is unknown to the reducer", () => {
    const state = { x: true };
    expect(reducer(state, { type: "EL_SPECIALE" })).toBe(state);
  });

  it("should store the permissions on FETCH_PERMISSION_SUCCESS", () => {
    const newState = reducer(
      {},
      fetchPermissionsSuccess(
        hitchhiker_puzzle42RepoPermissions,
        "hitchhiker",
        "puzzle42"
      )
    );

    expect(newState["hitchhiker/puzzle42"]).toBe(
      hitchhiker_puzzle42Permissions
    );
  });

  it("should update permission", () => {
    const oldState = {
      permissions: {
        "hitchhiker/puzzle42": {
          hitchhiker_puzzle42Permission_user_eins
        }
      }
    };
    let permissionEdited = { ...hitchhiker_puzzle42Permission_user_eins };
    permissionEdited.type = "OWNER";
    const newState = reducer(
      oldState,
      modifyPermissionSuccess(permissionEdited, "hitchhiker", "puzzle42")
    );
    expect(newState["hitchhiker/puzzle42"]).toEqual(permissionEdited);
  });
});

describe("permissions selectors", () => {
  const error = new Error("something goes wrong");

  it("should return the permissions of one repository", () => {
    const state = {
      permissions: {
        "hitchhiker/puzzle42": hitchhiker_puzzle42Permissions
      }
    };

    const repoPermissions = getPermissionsOfRepo(
      state,
      "hitchhiker",
      "puzzle42"
    );
    expect(repoPermissions).toEqual(hitchhiker_puzzle42Permissions);
  });

  it("should return true, when fetch permissions is pending", () => {
    const state = {
      pending: {
        [FETCH_PERMISSIONS + "/hitchhiker/puzzle42"]: true
      }
    };
    expect(isFetchPermissionsPending(state, "hitchhiker", "puzzle42")).toEqual(
      true
    );
  });

  it("should return false, when fetch permissions is not pending", () => {
    expect(isFetchPermissionsPending({}, "hitchiker", "puzzle42")).toEqual(
      false
    );
  });

  it("should return error when fetch permissions did fail", () => {
    const state = {
      failure: {
        [FETCH_PERMISSIONS + "/hitchhiker/puzzle42"]: error
      }
    };
    expect(getFetchPermissionsFailure(state, "hitchhiker", "puzzle42")).toEqual(
      error
    );
  });

  it("should return undefined when fetch permissions did not fail", () => {
    expect(getFetchPermissionsFailure({}, "hitchhiker", "puzzle42")).toBe(
      undefined
    );
  });
});
