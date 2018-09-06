// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type {
  PermissionCollection,
  Permission,
  PermissionEntry
} from "../types/Permissions";
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
export const CREATE_PERMISSION_RESET = `${CREATE_PERMISSION}_${
  types.RESET_SUFFIX
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
export const DELETE_PERMISSION_RESET = `${DELETE_PERMISSION}_${
  types.RESET_SUFFIX
}`;

const REPOS_URL = "repositories";
const PERMISSIONS_URL = "permissions";
const CONTENT_TYPE = "application/vnd.scmm-permission+json";

// fetch permissions

export function fetchPermissions(namespace: string, repoName: string) {
  return function(dispatch: any) {
    dispatch(fetchPermissionsPending(namespace, repoName));
    return apiClient
      .get(`${REPOS_URL}/${namespace}/${repoName}/${PERMISSIONS_URL}`)
      .then(response => response.json())
      .then(permissions => {
        dispatch(fetchPermissionsSuccess(permissions, namespace, repoName));
      })
      .catch(err => {
        dispatch(fetchPermissionsFailure(namespace, repoName, err));
      });
  };
}

export function fetchPermissionsPending(
  namespace: string,
  repoName: string
): Action {
  return {
    type: FETCH_PERMISSIONS_PENDING,
    payload: {
      namespace,
      repoName
    },
    itemId: namespace + "/" + repoName
  };
}

export function fetchPermissionsSuccess(
  permissions: any,
  namespace: string,
  repoName: string
): Action {
  return {
    type: FETCH_PERMISSIONS_SUCCESS,
    payload: permissions,
    itemId: namespace + "/" + repoName
  };
}

export function fetchPermissionsFailure(
  namespace: string,
  repoName: string,
  error: Error
): Action {
  return {
    type: FETCH_PERMISSIONS_FAILURE,
    payload: {
      namespace,
      repoName,
      error
    },
    itemId: namespace + "/" + repoName
  };
}

// modify permission

export function modifyPermission(
  permission: Permission,
  namespace: string,
  repoName: string,
  callback?: () => void
) {
  return function(dispatch: any) {
    dispatch(modifyPermissionPending(permission, namespace, repoName));
    return apiClient
      .put(permission._links.update.href, permission, CONTENT_TYPE)
      .then(() => {
        dispatch(modifyPermissionSuccess(permission, namespace, repoName));
        if (callback) {
          callback();
        }
      })
      .catch(cause => {
        const error = new Error(
          `failed to modify permission: ${cause.message}`
        );
        dispatch(
          modifyPermissionFailure(permission, error, namespace, repoName)
        );
      });
  };
}

export function modifyPermissionPending(
  permission: Permission,
  namespace: string,
  repoName: string
): Action {
  return {
    type: MODIFY_PERMISSION_PENDING,
    payload: permission,
    itemId: namespace + "/" + repoName + "/" + permission.name
  };
}

export function modifyPermissionSuccess(
  permission: Permission,
  namespace: string,
  repoName: string
): Action {
  return {
    type: MODIFY_PERMISSION_SUCCESS,
    payload: {
      permission,
      position: namespace + "/" + repoName
    },
    itemId: namespace + "/" + repoName + "/" + permission.name
  };
}

export function modifyPermissionFailure(
  permission: Permission,
  error: Error,
  namespace: string,
  repoName: string
): Action {
  return {
    type: MODIFY_PERMISSION_FAILURE,
    payload: { error, permission },
    itemId: namespace + "/" + repoName + "/" + permission.name
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
  repoName: string,
  permissionname: string
) {
  return {
    type: MODIFY_PERMISSION_RESET,
    itemId: namespace + "/" + repoName + "/" + permissionname
  };
}

// create permission
export function createPermission(
  permission: PermissionEntry,
  namespace: string,
  repoName: string,
  callback?: () => void
) {
  return function(dispatch: Dispatch) {
    dispatch(createPermissionPending(permission, namespace, repoName));
    return apiClient
      .post(
        `${REPOS_URL}/${namespace}/${repoName}/${PERMISSIONS_URL}`,
        permission,
        CONTENT_TYPE
      )
      .then(() => {
        dispatch(createPermissionSuccess(permission, namespace, repoName));
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
            repoName
          )
        )
      );
  };
}

export function createPermissionPending(
  permission: PermissionEntry,
  namespace: string,
  repoName: string
): Action {
  return {
    type: CREATE_PERMISSION_PENDING,
    payload: permission,
    itemId: namespace + "/" + repoName
  };
}

export function createPermissionSuccess(
  permission: PermissionEntry,
  namespace: string,
  repoName: string
): Action {
  return {
    type: CREATE_PERMISSION_SUCCESS,
    payload: {
      permission,
      position: namespace + "/" + repoName
    },
    itemId: namespace + "/" + repoName
  };
}

export function createPermissionFailure(
  error: Error,
  namespace: string,
  repoName: string
): Action {
  return {
    type: CREATE_PERMISSION_FAILURE,
    payload: error,
    itemId: namespace + "/" + repoName
  };
}

export function createPermissionReset(namespace: string, repoName: string) {
  return {
    type: CREATE_PERMISSION_RESET,
    itemId: namespace + "/" + repoName
  };
}

// delete permission

export function deletePermission(
  permission: Permission,
  namespace: string,
  repoName: string,
  callback?: () => void
) {
  return function(dispatch: any) {
    dispatch(deletePermissionPending(permission, namespace, repoName));
    return apiClient
      .delete(permission._links.delete.href)
      .then(() => {
        dispatch(deletePermissionSuccess(permission, namespace, repoName));
        if (callback) {
          callback();
        }
      })
      .catch(cause => {
        const error = new Error(
          `could not delete permission ${permission.name}: ${cause.message}`
        );
        dispatch(
          deletePermissionFailure(permission, namespace, repoName, error)
        );
      });
  };
}

export function deletePermissionPending(
  permission: Permission,
  namespace: string,
  repoName: string
): Action {
  return {
    type: DELETE_PERMISSION_PENDING,
    payload: permission,
    itemId: namespace + "/" + repoName + "/" + permission.name
  };
}

export function deletePermissionSuccess(
  permission: Permission,
  namespace: string,
  repoName: string
): Action {
  return {
    type: DELETE_PERMISSION_SUCCESS,
    payload: {
      permission,
      position: namespace + "/" + repoName
    },
    itemId: namespace + "/" + repoName + "/" + permission.name
  };
}

export function deletePermissionFailure(
  permission: Permission,
  namespace: string,
  repoName: string,
  error: Error
): Action {
  return {
    type: DELETE_PERMISSION_FAILURE,
    payload: {
      error,
      permission
    },
    itemId: namespace + "/" + repoName + "/" + permission.name
  };
}

export function deletePermissionReset(
  namespace: string,
  repoName: string,
  permissionname: string
) {
  return {
    type: DELETE_PERMISSION_RESET,
    itemId: namespace + "/" + repoName + "/" + permissionname
  };
}
function deletePermissionFromState(
  oldPermissions: PermissionCollection,
  permission: Permission
) {
  let newPermission = [];
  for (let i = 0; i < oldPermissions.length; i++) {
    if (oldPermissions[i] !== permission) {
      newPermission.push(oldPermissions[i]);
    }
  }
  return newPermission;
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
    case CREATE_PERMISSION_SUCCESS:
      return state;
    /*  const position = action.payload.position;
      const permissions = state[action.payload.position].entries;
      permissions.push(action.payload.permission);
      return {
        ...state,
        [position]: {
          ...state[position],
          entries: permissions
        }
      };*/
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
  repoName: string
) {
  if (state.permissions && state.permissions[namespace + "/" + repoName]) {
    const permissions = state.permissions[namespace + "/" + repoName].entries;
    return permissions;
  }
}

export function isFetchPermissionsPending(
  state: Object,
  namespace: string,
  repoName: string
) {
  return isPending(state, FETCH_PERMISSIONS, namespace + "/" + repoName);
}

export function getFetchPermissionsFailure(
  state: Object,
  namespace: string,
  repoName: string
) {
  return getFailure(state, FETCH_PERMISSIONS, namespace + "/" + repoName);
}

export function isModifyPermissionPending(
  state: Object,
  namespace: string,
  repoName: string,
  permissionname: string
) {
  return isPending(
    state,
    MODIFY_PERMISSION,
    namespace + "/" + repoName + "/" + permissionname
  );
}

export function getModifyPermissionFailure(
  state: Object,
  namespace: string,
  repoName: string,
  permissionname: string
) {
  return getFailure(
    state,
    MODIFY_PERMISSION,
    namespace + "/" + repoName + "/" + permissionname
  );
}

export function hasCreatePermission(
  state: Object,
  namespace: string,
  repoName: string
) {
  if (state.permissions && state.permissions[namespace + "/" + repoName])
    return state.permissions[namespace + "/" + repoName].createPermission;
  else return null;
}

export function isCreatePermissionPending(
  state: Object,
  namespace: string,
  repoName: string
) {
  return isPending(state, CREATE_PERMISSION, namespace + "/" + repoName);
}
export function getCreatePermissionFailure(
  state: Object,
  namespace: string,
  repoName: string
) {
  return getFailure(state, CREATE_PERMISSION, namespace + "/" + repoName);
}

export function isDeletePermissionPending(
  state: Object,
  namespace: string,
  repoName: string,
  permissionname: string
) {
  return isPending(
    state,
    DELETE_PERMISSION,
    namespace + "/" + repoName + "/" + permissionname
  );
}

export function getDeletePermissionFailure(
  state: Object,
  namespace: string,
  repoName: string,
  permissionname: string
) {
  return getFailure(
    state,
    DELETE_PERMISSION,
    namespace + "/" + repoName + "/" + permissionname
  );
}
