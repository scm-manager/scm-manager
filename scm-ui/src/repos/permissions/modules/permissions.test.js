// @flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  CREATE_PERMISSION,
  CREATE_PERMISSION_FAILURE,
  CREATE_PERMISSION_PENDING,
  CREATE_PERMISSION_SUCCESS,
  createPermission,
  createPermissionSuccess,
  DELETE_PERMISSION,
  DELETE_PERMISSION_FAILURE,
  DELETE_PERMISSION_PENDING,
  DELETE_PERMISSION_SUCCESS,
  deletePermission,
  deletePermissionSuccess,
  FETCH_PERMISSIONS,
  FETCH_PERMISSIONS_FAILURE,
  FETCH_PERMISSIONS_PENDING,
  FETCH_PERMISSIONS_SUCCESS,
  fetchPermissions,
  fetchPermissionsSuccess,
  getCreatePermissionFailure,
  getDeletePermissionFailure,
  getDeletePermissionsFailure,
  getFetchPermissionsFailure,
  getModifyPermissionFailure,
  getModifyPermissionsFailure,
  getPermissionsOfRepo,
  hasCreatePermission,
  isCreatePermissionPending,
  isDeletePermissionPending,
  isFetchPermissionsPending,
  isModifyPermissionPending,
  MODIFY_PERMISSION,
  MODIFY_PERMISSION_FAILURE,
  MODIFY_PERMISSION_PENDING,
  MODIFY_PERMISSION_SUCCESS,
  modifyPermission,
  modifyPermissionSuccess
} from "./permissions";
import type {Permission, PermissionCollection} from "@scm-manager/ui-types";

const hitchhiker_puzzle42Permission_user_eins: Permission = {
  name: "user_eins",
  type: "READ",
  groupPermission: false,
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
  },
  verbs: []
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
  },
  verbs: []
};

const hitchhiker_puzzle42Permissions: PermissionCollection = [
  hitchhiker_puzzle42Permission_user_eins,
  hitchhiker_puzzle42Permission_user_zwei
];

const hitchhiker_puzzle42RepoPermissions = {
  _embedded: {
    permissions: hitchhiker_puzzle42Permissions
  },
  _links: {
    create: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/hitchhiker/puzzle42/permissions"
    }
  }
};

describe("permission fetch", () => {
  const REPOS_URL = "/api/v2/repositories";
  const URL = "repositories";
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
          repoName: "puzzle42"
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
      .dispatch(
        fetchPermissions(
          URL + "/hitchhiker/puzzle42/permissions",
          "hitchhiker",
          "puzzle42"
        )
      )
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
      .dispatch(
        fetchPermissions(
          URL + "/hitchhiker/puzzle42/permissions",
          "hitchhiker",
          "puzzle42"
        )
      )
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

    let editedPermission = { ...hitchhiker_puzzle42Permission_user_eins, type: "OWNER" };

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

    let editedPermission = { ...hitchhiker_puzzle42Permission_user_eins, type: "OWNER" };

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

    let editedPermission = { ...hitchhiker_puzzle42Permission_user_eins, type: "OWNER" };

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

  it("should add a permission successfully", () => {
    // unmatched
    fetchMock.postOnce(REPOS_URL + "/hitchhiker/puzzle42/permissions", {
      status: 204,
      headers: {
        location: "repositories/hitchhiker/puzzle42/permissions/user_eins"
      }
    });

    fetchMock.getOnce(
      REPOS_URL + "/hitchhiker/puzzle42/permissions/user_eins",
      hitchhiker_puzzle42Permission_user_eins
    );

    const store = mockStore({});
    return store
      .dispatch(
        createPermission(
          URL + "/hitchhiker/puzzle42/permissions",
          hitchhiker_puzzle42Permission_user_eins,
          "hitchhiker",
          "puzzle42"
        )
      )
      .then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(CREATE_PERMISSION_PENDING);
        expect(actions[1].type).toEqual(CREATE_PERMISSION_SUCCESS);
      });
  });

  it("should fail adding a permission on HTTP 500", () => {
    fetchMock.postOnce(REPOS_URL + "/hitchhiker/puzzle42/permissions", {
      status: 500
    });

    const store = mockStore({});
    return store
      .dispatch(
        createPermission(
          URL + "/hitchhiker/puzzle42/permissions",
          hitchhiker_puzzle42Permission_user_eins,
          "hitchhiker",
          "puzzle42"
        )
      )
      .then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(CREATE_PERMISSION_PENDING);
        expect(actions[1].type).toEqual(CREATE_PERMISSION_FAILURE);
        expect(actions[1].payload).toBeDefined();
      });
  });

  it("should call the callback after permission successfully created", () => {
    // unmatched
    fetchMock.postOnce(REPOS_URL + "/hitchhiker/puzzle42/permissions", {
      status: 204,
      headers: {
        location: "repositories/hitchhiker/puzzle42/permissions/user_eins"
      }
    });

    fetchMock.getOnce(
      REPOS_URL + "/hitchhiker/puzzle42/permissions/user_eins",
      hitchhiker_puzzle42Permission_user_eins
    );
    let callMe = "not yet";

    const callback = () => {
      callMe = "yeah";
    };

    const store = mockStore({});
    return store
      .dispatch(
        createPermission(
          URL + "/hitchhiker/puzzle42/permissions",
          hitchhiker_puzzle42Permission_user_eins,
          "hitchhiker",
          "puzzle42",
          callback
        )
      )
      .then(() => {
        expect(callMe).toBe("yeah");
      });
  });
  it("should delete successfully permission user_eins", () => {
    fetchMock.deleteOnce(
      hitchhiker_puzzle42Permission_user_eins._links.delete.href,
      {
        status: 204
      }
    );

    const store = mockStore({});
    return store
      .dispatch(
        deletePermission(
          hitchhiker_puzzle42Permission_user_eins,
          "hitchhiker",
          "puzzle42"
        )
      )
      .then(() => {
        const actions = store.getActions();
        expect(actions.length).toBe(2);
        expect(actions[0].type).toEqual(DELETE_PERMISSION_PENDING);
        expect(actions[0].payload).toBe(
          hitchhiker_puzzle42Permission_user_eins
        );
        expect(actions[1].type).toEqual(DELETE_PERMISSION_SUCCESS);
      });
  });

  it("should call the callback, after successful delete", () => {
    fetchMock.deleteOnce(
      hitchhiker_puzzle42Permission_user_eins._links.delete.href,
      {
        status: 204
      }
    );

    let called = false;
    const callMe = () => {
      called = true;
    };

    const store = mockStore({});
    return store
      .dispatch(
        deletePermission(
          hitchhiker_puzzle42Permission_user_eins,
          "hitchhiker",
          "puzzle42",
          callMe
        )
      )
      .then(() => {
        expect(called).toBeTruthy();
      });
  });

  it("should fail to delete permission", () => {
    fetchMock.deleteOnce(
      hitchhiker_puzzle42Permission_user_eins._links.delete.href,
      {
        status: 500
      }
    );

    const store = mockStore({});
    return store
      .dispatch(
        deletePermission(
          hitchhiker_puzzle42Permission_user_eins,
          "hitchhiker",
          "puzzle42"
        )
      )
      .then(() => {
        const actions = store.getActions();
        expect(actions[0].type).toEqual(DELETE_PERMISSION_PENDING);
        expect(actions[0].payload).toBe(
          hitchhiker_puzzle42Permission_user_eins
        );
        expect(actions[1].type).toEqual(DELETE_PERMISSION_FAILURE);
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

    expect(newState["hitchhiker/puzzle42"].entries).toBe(
      hitchhiker_puzzle42Permissions
    );
  });

  it("should update permission", () => {
    const oldState = {
      "hitchhiker/puzzle42": {
        entries: [hitchhiker_puzzle42Permission_user_eins]
      }
    };
    let permissionEdited = { ...hitchhiker_puzzle42Permission_user_eins, type: "OWNER" };
    let expectedState = {
      "hitchhiker/puzzle42": {
        entries: [permissionEdited]
      }
    };
    const newState = reducer(
      oldState,
      modifyPermissionSuccess(permissionEdited, "hitchhiker", "puzzle42")
    );
    expect(newState["hitchhiker/puzzle42"]).toEqual(
      expectedState["hitchhiker/puzzle42"]
    );
  });

  it("should remove permission from state when delete succeeds", () => {
    const state = {
      "hitchhiker/puzzle42": {
        entries: [
          hitchhiker_puzzle42Permission_user_eins,
          hitchhiker_puzzle42Permission_user_zwei
        ]
      }
    };

    const expectedState = {
      "hitchhiker/puzzle42": {
        entries: [hitchhiker_puzzle42Permission_user_zwei]
      }
    };

    const newState = reducer(
      state,
      deletePermissionSuccess(
        hitchhiker_puzzle42Permission_user_eins,
        "hitchhiker",
        "puzzle42"
      )
    );
    expect(newState["hitchhiker/puzzle42"]).toEqual(
      expectedState["hitchhiker/puzzle42"]
    );
  });

  it("should add permission", () => {
    //changing state had to be removed because of errors
    const oldState = {
      "hitchhiker/puzzle42": {
        entries: [hitchhiker_puzzle42Permission_user_eins]
      }
    };
    let expectedState = {
      "hitchhiker/puzzle42": {
        entries: [
          hitchhiker_puzzle42Permission_user_eins,
          hitchhiker_puzzle42Permission_user_zwei
        ]
      }
    };
    const newState = reducer(
      oldState,
      createPermissionSuccess(
        hitchhiker_puzzle42Permission_user_zwei,
        "hitchhiker",
        "puzzle42"
      )
    );
    expect(newState["hitchhiker/puzzle42"]).toEqual(
      expectedState["hitchhiker/puzzle42"]
    );
  });
});

describe("permissions selectors", () => {
  const error = new Error("something goes wrong");

  it("should return the permissions of one repository", () => {
    const state = {
      permissions: {
        "hitchhiker/puzzle42": {
          entries: hitchhiker_puzzle42Permissions
        }
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

  it("should return true, when modify permission is pending", () => {
    const state = {
      pending: {
        [MODIFY_PERMISSION + "/hitchhiker/puzzle42/user_eins"]: true
      }
    };
    expect(
      isModifyPermissionPending(
        state,
        "hitchhiker",
        "puzzle42",
        hitchhiker_puzzle42Permission_user_eins
      )
    ).toEqual(true);
  });

  it("should return false, when modify permission is not pending", () => {
    expect(
      isModifyPermissionPending(
        {},
        "hitchiker",
        "puzzle42",
        hitchhiker_puzzle42Permission_user_eins
      )
    ).toEqual(false);
  });

  it("should return error when modify permission did fail", () => {
    const state = {
      failure: {
        [MODIFY_PERMISSION + "/hitchhiker/puzzle42/user_eins"]: error
      }
    };
    expect(
      getModifyPermissionFailure(
        state,
        "hitchhiker",
        "puzzle42",
        hitchhiker_puzzle42Permission_user_eins
      )
    ).toEqual(error);
  });

  it("should return undefined when modify permission did not fail", () => {
    expect(
      getModifyPermissionFailure(
        {},
        "hitchhiker",
        "puzzle42",
        hitchhiker_puzzle42Permission_user_eins
      )
    ).toBe(undefined);
  });

  it("should return error when one of the modify permissions did fail", () => {
    const state = {
      permissions: {
        "hitchhiker/puzzle42": { entries: hitchhiker_puzzle42Permissions }
      },
      failure: {
        [MODIFY_PERMISSION + "/hitchhiker/puzzle42/user_eins"]: error
      }
    };
    expect(
      getModifyPermissionsFailure(state, "hitchhiker", "puzzle42")
    ).toEqual(error);
  });

  it("should return undefined when no modify permissions did not fail", () => {
    expect(getModifyPermissionsFailure({}, "hitchhiker", "puzzle42")).toBe(
      undefined
    );
  });

  it("should return true, when createPermission is true", () => {
    const state = {
      permissions: {
        "hitchhiker/puzzle42": {
          createPermission: true
        }
      }
    };
    expect(hasCreatePermission(state, "hitchhiker", "puzzle42")).toBe(true);
  });

  it("should return false, when createPermission is false", () => {
    const state = {
      permissions: {
        "hitchhiker/puzzle42": {
          createPermission: false
        }
      }
    };
    expect(hasCreatePermission(state, "hitchhiker", "puzzle42")).toEqual(false);
  });

  it("should return true, when delete permission is pending", () => {
    const state = {
      pending: {
        [DELETE_PERMISSION + "/hitchhiker/puzzle42/user_eins"]: true
      }
    };
    expect(
      isDeletePermissionPending(
        state,
        "hitchhiker",
        "puzzle42",
        hitchhiker_puzzle42Permission_user_eins
      )
    ).toEqual(true);
  });

  it("should return false, when delete permission is not pending", () => {
    expect(
      isDeletePermissionPending(
        {},
        "hitchiker",
        "puzzle42",
        hitchhiker_puzzle42Permission_user_eins
      )
    ).toEqual(false);
  });

  it("should return error when delete permission did fail", () => {
    const state = {
      failure: {
        [DELETE_PERMISSION + "/hitchhiker/puzzle42/user_eins"]: error
      }
    };
    expect(
      getDeletePermissionFailure(
        state,
        "hitchhiker",
        "puzzle42",
        hitchhiker_puzzle42Permission_user_eins
      )
    ).toEqual(error);
  });

  it("should return undefined when delete permission did not fail", () => {
    expect(
      getDeletePermissionFailure(
        {},
        "hitchhiker",
        "puzzle42",
        hitchhiker_puzzle42Permission_user_eins
      )
    ).toBe(undefined);
  });

  it("should return error when one of the delete permissions did fail", () => {
    const state = {
      permissions: {
        "hitchhiker/puzzle42": { entries: hitchhiker_puzzle42Permissions }
      },
      failure: {
        [DELETE_PERMISSION + "/hitchhiker/puzzle42/user_eins"]: error
      }
    };
    expect(
      getDeletePermissionsFailure(state, "hitchhiker", "puzzle42")
    ).toEqual(error);
  });

  it("should return undefined when no delete permissions did not fail", () => {
    expect(getDeletePermissionsFailure({}, "hitchhiker", "puzzle42")).toBe(
      undefined
    );
  });

  it("should return true, when create permission is pending", () => {
    const state = {
      pending: {
        [CREATE_PERMISSION + "/hitchhiker/puzzle42"]: true
      }
    };
    expect(isCreatePermissionPending(state, "hitchhiker", "puzzle42")).toEqual(
      true
    );
  });

  it("should return false, when create permissions is not pending", () => {
    expect(isCreatePermissionPending({}, "hitchiker", "puzzle42")).toEqual(
      false
    );
  });

  it("should return error when create permissions did fail", () => {
    const state = {
      failure: {
        [CREATE_PERMISSION + "/hitchhiker/puzzle42"]: error
      }
    };
    expect(getCreatePermissionFailure(state, "hitchhiker", "puzzle42")).toEqual(
      error
    );
  });

  it("should return undefined when create permissions did not fail", () => {
    expect(getCreatePermissionFailure({}, "hitchhiker", "puzzle42")).toBe(
      undefined
    );
  });
});
