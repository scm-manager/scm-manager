// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type { PermissionCollection, Permission } from "../types/Permissions";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import { Dispatch } from "redux";

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
export const MODIFY_PERMISSION_RESET = `${MODIFY_PERMISSION}_${
  types.RESET_SUFFIX
}`;
export const CREATE_PERMISSION = "scm/permissions/CREATE_PERMISSION";
export const CREATE_PERMISSION_PENDING = `${CREATE_PERMISSION}_${
  types.PENDING_SUFFIX
}`;
export const CREATE_PERMISSION_SUCCESS = `${CREATE_PERMISSION}_${
  types.SUCCESS_SUFFIX
}`;
export const CREATE_PERMISSION_FAILURE = `${CREATE_PERMISSION}_${
  types.FAILURE_SUFFIX
}`;
export const DELETE_PERMISSION = "scm/permissions/DELETE_PERMISSION";
export const DELETE_PERMISSION_PENDING = `${DELETE_PERMISSION}_${
  types.PENDING_SUFFIX
}`;
export const DELETE_PERMISSION_SUCCESS = `${DELETE_PERMISSION}_${
  types.SUCCESS_SUFFIX
}`;
export const DELETE_PERMISSION_FAILURE = `${DELETE_PERMISSION}_${
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
    dispatch(modifyPermissionPending(permission, namespace, name));
    return apiClient
      .put(permission._links.update.href, permission, CONTENT_TYPE)
      .then(() => {
        dispatch(modifyPermissionSuccess(permission, namespace, name));
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
      permission,
      position: namespace + "/" + name
    },
    itemId: namespace + "/" + name + "/" + permission.name
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
  for (let i = 0; i < oldPermissions.length; i++) {
    if (oldPermissions[i].name === newPermission.name) {
      oldPermissions.splice(i, 1, newPermission);
      return oldPermissions;
    }
  }
}

export function modifyPermissionReset(
  namespace: string,
  name: string,
  permissionname: string
) {
  return {
    type: MODIFY_PERMISSION_RESET,
    itemId: namespace + "/" + name + "/" + permissionname
  };
}

// create permission
export function createPermission(
  permission: Permission,
  namespace: string,
  name: string,
  callback?: () => void
) {
  return function(dispatch: Dispatch) {
    dispatch(createPermissionPending(permission, namespace, name));
    return apiClient
      .post(
        `${REPOS_URL}/${namespace}/${name}/${PERMISSIONS_URL}`,
        permission,
        CONTENT_TYPE
      )
      .then(() => {
        dispatch(createPermissionSuccess(namespace, name));
        if (callback) {
          callback();
        }
      })
      .catch(err =>
        dispatch(
          createPermissionFailure(
            new Error(
              `failed to add permission ${permission.name}: ${err.message}`
            ),
            namespace,
            name
          )
        )
      );
  };
}

export function createPermissionPending(
  permission: Permission,
  namespace: string,
  name: string
): Action {
  return {
    type: CREATE_PERMISSION_PENDING,
    payload: permission,
    itemId: namespace + "/" + name
  };
}

export function createPermissionSuccess(
  namespace: string,
  name: string
): Action {
  return {
    type: CREATE_PERMISSION_SUCCESS,
    itemId: namespace + "/" + name
  };
}

export function createPermissionFailure(
  error: Error,
  namespace: string,
  name: string
): Action {
  return {
    type: CREATE_PERMISSION_FAILURE,
    payload: error,
    itemId: namespace + "/" + name
  };
}

// delete permission

export function deletePermission(
  permission: Permission,
  namespace: string,
  name: string,
  callback?: () => void
) {
  return function(dispatch: any) {
    dispatch(deletePermissionPending(permission, namespace, name));
    return apiClient
      .delete(permission._links.delete.href)
      .then(() => {
        dispatch(deletePermissionSuccess(permission, namespace, name));
        if (callback) {
          callback();
        }
      })
      .catch(cause => {
        const error = new Error(
          `could not delete permission ${permission.name}: ${cause.message}`
        );
        dispatch(deletePermissionFailure(permission, namespace, name, error));
      });
  };
}

export function deletePermissionPending(
  permission: Permission,
  namespace: string,
  name: string
): Action {
  return {
    type: DELETE_PERMISSION_PENDING,
    payload: permission,
    itemId: namespace + "/" + name + "/" + permission.name
  };
}

export function deletePermissionSuccess(
  permission: Permission,
  namespace: string,
  name: string
): Action {
  return {
    type: DELETE_PERMISSION_SUCCESS,
    payload: {
      permission,
      position: namespace + "/" + name
    },
    itemId: namespace + "/" + name + "/" + permission.name
  };
}

export function deletePermissionFailure(
  permission: Permission,
  namespace: string,
  name: string,
  error: Error
): Action {
  return {
    type: DELETE_PERMISSION_FAILURE,
    payload: {
      error,
      permission
    },
    itemId: namespace + "/" + name + "/" + permission.name
  };
}

function deletePermissionFromState(
  oldPermissions: PermissionCollection,
  permission: Permission
) {
  for (let i = 0; i < oldPermissions.length; i++) {
    if (oldPermissions[i] === permission) {
      oldPermissions.splice(i, 1);
    }
  }
  return oldPermissions;
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
        [action.itemId]: {
          entries: action.payload._embedded.permissions,
          createPermission: action.payload._links.create ? true : false
        }
      };
    case MODIFY_PERMISSION_SUCCESS:
      const positionOfPermission = action.payload.position;
      const newPermission = newPermissions(
        state[action.payload.position].entries,
        action.payload.permission
      );
      return {
        ...state,
        [positionOfPermission]: {
          ...state[positionOfPermission],
          entries: newPermission
        }
      };
    case DELETE_PERMISSION_SUCCESS:
      const permissionPosition = action.payload.position;
      const new_Permissions = deletePermissionFromState(
        state[action.payload.position].entries,
        action.payload.permission
      );
      return {
        ...state,
        [permissionPosition]: {
          ...state[permissionPosition],
          entries: new_Permissions
        }
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
    const permissions = state.permissions[namespace + "/" + name].entries;
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

export function hasCreatePermission(
  state: Object,
  namespace: string,
  name: string
) {
  if (state.permissions && state.permissions[namespace + "/" + name])
    return state.permissions[namespace + "/" + name].createPermission;
  else return null;
}
