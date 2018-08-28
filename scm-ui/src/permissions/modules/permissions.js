// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type { PermissionCollection, Permission } from "../types/Permissions";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";

export const FETCH_PERMISSIONS = "scm/permissions/FETCH_PERMISSIONS";
export const FETCH_PERMISSIONS_PENDING = `${FETCH_PERMISSIONS}_${
  types.PENDING_SUFFIX
}`;
export const FETCH_PERMISSIONS_SUCCESS = `${FETCH_PERMISSIONS}_${
  types.SUCCESS_SUFFIX
}`;
export const FETCH_PERMISSIONS_FAILURE = `${FETCH_PERMISSIONS}_${
  types.FAILURE_SUFFIX
}`;
export const MODIFY_PERMISSION = "scm/permissions/MODFIY_PERMISSION";
export const MODIFY_PERMISSION_PENDING = `${MODIFY_PERMISSION}_${
  types.PENDING_SUFFIX
}`;
export const MODIFY_PERMISSION_SUCCESS = `${MODIFY_PERMISSION}_${
  types.SUCCESS_SUFFIX
}`;
export const MODIFY_PERMISSION_FAILURE = `${MODIFY_PERMISSION}_${
  types.FAILURE_SUFFIX
}`;
const REPOS_URL = "repositories";
const PERMISSIONS_URL = "permissions";
const CONTENT_TYPE = "application/vnd.scmm-permission+json";

// fetch permissions

export function fetchPermissions(namespace: string, name: string) {
  return function(dispatch: any) {
    dispatch(fetchPermissionsPending(namespace, name));
    return apiClient
      .get(`${REPOS_URL}/${namespace}/${name}/${PERMISSIONS_URL}`)
      .then(response => response.json())
      .then(permissions => {
        dispatch(fetchPermissionsSuccess(permissions, namespace, name));
      })
      .catch(err => {
        dispatch(fetchPermissionsFailure(namespace, name, err));
      });
  };
}

export function fetchPermissionsPending(
  namespace: string,
  name: string
): Action {
  return {
    type: FETCH_PERMISSIONS_PENDING,
    payload: {
      namespace,
      name
    },
    itemId: namespace + "/" + name
  };
}

export function fetchPermissionsSuccess(
  permissions: any,
  namespace: string,
  name: string
): Action {
  return {
    type: FETCH_PERMISSIONS_SUCCESS,
    payload: permissions,
    itemId: namespace + "/" + name
  };
}

export function fetchPermissionsFailure(
  namespace: string,
  name: string,
  error: Error
): Action {
  return {
    type: FETCH_PERMISSIONS_FAILURE,
    payload: {
      namespace,
      name,
      error
    },
    itemId: namespace + "/" + name
  };
}

// modify permission

export function modifyPermission(
  permission: Permission,
  namespace: string,
  name: string,
  callback?: () => void
) {
  return function(dispatch: any) {
    dispatch(
      modifyPermissionPending(permission, namespace, name)
    );
    return apiClient
      .put(permission._links.update.href, permission, CONTENT_TYPE)
      .then(() => {
        dispatch(
          modifyPermissionSuccess(permission, namespace, name)
        );
        if (callback) {
          callback();
        }
      })
      .catch(cause => {
        const error = new Error(
          `failed to modify permission: ${cause.message}`
        );
        dispatch(modifyPermissionFailure(permission, error, namespace, name));
      });
  };
}

export function modifyPermissionPending(
  permission: Permission,
  namespace: string,
  name: string
): Action {
  return {
    type: MODIFY_PERMISSION_PENDING,
    payload: permission,
    itemId: namespace + "/" + name + "/" + permission.name
  };
}

export function modifyPermissionSuccess(
  permission: Permission,
  namespace: string,
  name: string
): Action {
  return {
    type: MODIFY_PERMISSION_SUCCESS,
    payload: {
      permission
    },
    itemId: namespace + "/" + name
  };
}

export function modifyPermissionFailure(
  permission: Permission,
  error: Error,
  namespace: string,
  name: string
): Action {
  return {
    type: MODIFY_PERMISSION_FAILURE,
    payload: { error, permission },
    itemId: namespace + "/" + name + "/" + permission.name
  };
}

function newPermissions(
  oldPermissions: PermissionCollection,
  newPermission: Permission
) {
  console.log("oldPermissions:");
  console.log(oldPermissions);
  console.log("new Permission:");
  console.log(newPermission);
  for (let i = 0; i < oldPermissions.length; i++) {
    console.log("permissionname:");
    console.log("an der stelle i: " + oldPermissions[i].name);
    if (oldPermissions[i].name === newPermission.name) {
      oldPermissions.splice(i, 1, newPermission);
      console.log("new Permissions");
      console.log(oldPermissions);
      return oldPermissions;
    }
  }
}

// reducer
export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  if (!action.payload) {
    return state;
  }

  switch (action.type) {
    case FETCH_PERMISSIONS_SUCCESS:
      return {
        ...state,
        [action.itemId]: action.payload._embedded.permissions
      };
    case MODIFY_PERMISSION_SUCCESS:
      const newPermission = newPermissions(
        state[action.itemId],
        action.payload.permission
      );
      return {
        ...state,
        [action.itemId]: newPermission
      };
    default:
      return state;
  }
}

// selectors

export function getPermissionsOfRepo(
  state: Object,
  namespace: string,
  name: string
) {
  if (state.permissions && state.permissions[namespace + "/" + name]) {
    const permissions = state.permissions[namespace + "/" + name];
    return permissions;
  }
}

export function isFetchPermissionsPending(
  state: Object,
  namespace: string,
  name: string
) {
  return isPending(state, FETCH_PERMISSIONS, namespace + "/" + name);
}

export function getFetchPermissionsFailure(
  state: Object,
  namespace: string,
  name: string
) {
  return getFailure(state, FETCH_PERMISSIONS, namespace + "/" + name);
}

export function isModifyPermissionPending(
  state: Object,
  namespace: string,
  name: string,
  permissionname: string
) {
  return isPending(
    state,
    MODIFY_PERMISSION,
    namespace + "/" + name + "/" + permissionname
  );
}

export function getModifyPermissionFailure(
  state: Object,
  namespace: string,
  name: string,
  permissionname: string
) {
  return getFailure(
    state,
    MODIFY_PERMISSION,
    namespace + "/" + name + "/" + permissionname
  );
}
