/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import { Action, apiClient } from "@scm-manager/ui-components";
import * as types from "../../../modules/types";
import { Permission, PermissionCollection, PermissionCreateEntry, RepositoryRole } from "@scm-manager/ui-types";
import { isPending } from "../../../modules/pending";
import { getFailure } from "../../../modules/failure";
import { Dispatch } from "redux";

export const FETCH_AVAILABLE = "scm/permissions/FETCH_AVAILABLE";
export const FETCH_AVAILABLE_PENDING = `${FETCH_AVAILABLE}_${types.PENDING_SUFFIX}`;
export const FETCH_AVAILABLE_SUCCESS = `${FETCH_AVAILABLE}_${types.SUCCESS_SUFFIX}`;
export const FETCH_AVAILABLE_FAILURE = `${FETCH_AVAILABLE}_${types.FAILURE_SUFFIX}`;
export const FETCH_PERMISSIONS = "scm/permissions/FETCH_PERMISSIONS";
export const FETCH_PERMISSIONS_PENDING = `${FETCH_PERMISSIONS}_${types.PENDING_SUFFIX}`;
export const FETCH_PERMISSIONS_SUCCESS = `${FETCH_PERMISSIONS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_PERMISSIONS_FAILURE = `${FETCH_PERMISSIONS}_${types.FAILURE_SUFFIX}`;
export const MODIFY_PERMISSION = "scm/permissions/MODFIY_PERMISSION";
export const MODIFY_PERMISSION_PENDING = `${MODIFY_PERMISSION}_${types.PENDING_SUFFIX}`;
export const MODIFY_PERMISSION_SUCCESS = `${MODIFY_PERMISSION}_${types.SUCCESS_SUFFIX}`;
export const MODIFY_PERMISSION_FAILURE = `${MODIFY_PERMISSION}_${types.FAILURE_SUFFIX}`;
export const MODIFY_PERMISSION_RESET = `${MODIFY_PERMISSION}_${types.RESET_SUFFIX}`;
export const CREATE_PERMISSION = "scm/permissions/CREATE_PERMISSION";
export const CREATE_PERMISSION_PENDING = `${CREATE_PERMISSION}_${types.PENDING_SUFFIX}`;
export const CREATE_PERMISSION_SUCCESS = `${CREATE_PERMISSION}_${types.SUCCESS_SUFFIX}`;
export const CREATE_PERMISSION_FAILURE = `${CREATE_PERMISSION}_${types.FAILURE_SUFFIX}`;
export const CREATE_PERMISSION_RESET = `${CREATE_PERMISSION}_${types.RESET_SUFFIX}`;
export const DELETE_PERMISSION = "scm/permissions/DELETE_PERMISSION";
export const DELETE_PERMISSION_PENDING = `${DELETE_PERMISSION}_${types.PENDING_SUFFIX}`;
export const DELETE_PERMISSION_SUCCESS = `${DELETE_PERMISSION}_${types.SUCCESS_SUFFIX}`;
export const DELETE_PERMISSION_FAILURE = `${DELETE_PERMISSION}_${types.FAILURE_SUFFIX}`;
export const DELETE_PERMISSION_RESET = `${DELETE_PERMISSION}_${types.RESET_SUFFIX}`;

const CONTENT_TYPE = "application/vnd.scmm-repositoryPermission+json";

// fetch available permissions

export function fetchAvailablePermissionsIfNeeded(repositoryRolesLink: string, repositoryVerbsLink: string) {
  return function(dispatch: any, getState: () => object) {
    if (shouldFetchAvailablePermissions(getState())) {
      return fetchAvailablePermissions(dispatch, getState, repositoryRolesLink, repositoryVerbsLink);
    }
  };
}

export function fetchAvailablePermissions(
  dispatch: any,
  getState: () => object,
  repositoryRolesLink: string,
  repositoryVerbsLink: string
) {
  dispatch(fetchAvailablePending());
  return apiClient
    .get(repositoryRolesLink)
    .then(repositoryRoles => repositoryRoles.json())
    .then(repositoryRoles => repositoryRoles._embedded.repositoryRoles)
    .then(repositoryRoles => {
      return apiClient
        .get(repositoryVerbsLink)
        .then(repositoryVerbs => repositoryVerbs.json())
        .then(repositoryVerbs => repositoryVerbs.verbs)
        .then(repositoryVerbs => {
          return {
            repositoryVerbs,
            repositoryRoles
          };
        });
    })
    .then(available => {
      dispatch(fetchAvailableSuccess(available));
    })
    .catch(err => {
      dispatch(fetchAvailableFailure(err));
    });
}

export function shouldFetchAvailablePermissions(state: object) {
  if (isFetchAvailablePermissionsPending(state) || getFetchAvailablePermissionsFailure(state)) {
    return false;
  }
  return !state.available;
}

export function fetchAvailablePending(): Action {
  return {
    type: FETCH_AVAILABLE_PENDING,
    payload: {},
    itemId: "available"
  };
}

export function fetchAvailableSuccess(available: [RepositoryRole[], string[]]): Action {
  return {
    type: FETCH_AVAILABLE_SUCCESS,
    payload: available,
    itemId: "available"
  };
}

export function fetchAvailableFailure(error: Error): Action {
  return {
    type: FETCH_AVAILABLE_FAILURE,
    payload: {
      error
    },
    itemId: "available"
  };
}

// fetch permissions

export function fetchPermissions(link: string, namespace: string, repoName?: string) {
  return function(dispatch: any) {
    dispatch(fetchPermissionsPending(namespace, repoName));
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(permissions => {
        dispatch(fetchPermissionsSuccess(permissions, namespace, repoName));
      })
      .catch(err => {
        dispatch(fetchPermissionsFailure(namespace, repoName, err));
      });
  };
}

export function fetchPermissionsPending(namespace: string, repoName?: string): Action {
  return {
    type: FETCH_PERMISSIONS_PENDING,
    payload: {
      namespace,
      repoName
    },
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

export function fetchPermissionsSuccess(permissions: any, namespace: string, repoName?: string): Action {
  return {
    type: FETCH_PERMISSIONS_SUCCESS,
    payload: permissions,
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

export function fetchPermissionsFailure(namespace: string, repoName?: string, error: Error): Action {
  return {
    type: FETCH_PERMISSIONS_FAILURE,
    payload: {
      namespace,
      repoName,
      error
    },
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

// modify permission

export function modifyPermission(permission: Permission, namespace: string, repoName?: string, callback?: () => void) {
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
      .catch(err => {
        dispatch(modifyPermissionFailure(permission, err, namespace, repoName));
      });
  };
}

export function modifyPermissionPending(permission: Permission, namespace: string, repoName?: string): Action {
  return {
    type: MODIFY_PERMISSION_PENDING,
    payload: permission,
    itemId: createItemId(permission, namespace, repoName)
  };
}

export function modifyPermissionSuccess(permission: Permission, namespace: string, repoName?: string): Action {
  return {
    type: MODIFY_PERMISSION_SUCCESS,
    payload: {
      permission,
      position: createPermissionStateKey(namespace, repoName)
    },
    itemId: createItemId(permission, namespace, repoName)
  };
}

export function modifyPermissionFailure(
  permission: Permission,
  error: Error,
  namespace: string,
  repoName?: string
): Action {
  return {
    type: MODIFY_PERMISSION_FAILURE,
    payload: {
      error,
      permission
    },
    itemId: createItemId(permission, namespace, repoName)
  };
}

function newPermissions(oldPermissions: PermissionCollection, newPermission: Permission) {
  for (let i = 0; i < oldPermissions.length; i++) {
    if (oldPermissions[i].name === newPermission.name) {
      oldPermissions.splice(i, 1, newPermission);
      return oldPermissions;
    }
  }
}

export function modifyPermissionReset(namespace: string, repoName?: string) {
  return {
    type: MODIFY_PERMISSION_RESET,
    payload: {
      namespace,
      repoName
    },
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

// create permission
export function createPermission(
  link: string,
  permission: PermissionCreateEntry,
  namespace: string,
  repoName?: string,
  callback?: () => void
) {
  return function(dispatch: Dispatch) {
    dispatch(createPermissionPending(permission, namespace, repoName));
    return apiClient
      .post(link, permission, CONTENT_TYPE)
      .then(response => {
        const location = response.headers.get("Location");
        return apiClient.get(location);
      })
      .then(response => response.json())
      .then(createdPermission => {
        dispatch(createPermissionSuccess(createdPermission, namespace, repoName));
        if (callback) {
          callback();
        }
      })
      .catch(err => dispatch(createPermissionFailure(err, namespace, repoName)));
  };
}

export function createPermissionPending(
  permission: PermissionCreateEntry,
  namespace: string,
  repoName?: string
): Action {
  return {
    type: CREATE_PERMISSION_PENDING,
    payload: permission,
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

export function createPermissionSuccess(
  permission: PermissionCreateEntry,
  namespace: string,
  repoName?: string
): Action {
  return {
    type: CREATE_PERMISSION_SUCCESS,
    payload: {
      permission,
      position: createPermissionStateKey(namespace, repoName)
    },
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

export function createPermissionFailure(error: Error, namespace: string, repoName?: string): Action {
  return {
    type: CREATE_PERMISSION_FAILURE,
    payload: error,
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

export function createPermissionReset(namespace: string, repoName?: string) {
  return {
    type: CREATE_PERMISSION_RESET,
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

// delete permission

export function deletePermission(permission: Permission, namespace: string, repoName?: string, callback?: () => void) {
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
      .catch(err => {
        dispatch(deletePermissionFailure(permission, namespace, repoName, err));
      });
  };
}

export function deletePermissionPending(permission: Permission, namespace: string, repoName?: string): Action {
  return {
    type: DELETE_PERMISSION_PENDING,
    payload: permission,
    itemId: createItemId(permission, namespace, repoName)
  };
}

export function deletePermissionSuccess(permission: Permission, namespace: string, repoName?: string): Action {
  return {
    type: DELETE_PERMISSION_SUCCESS,
    payload: {
      permission,
      position: createPermissionStateKey(namespace, repoName)
    },
    itemId: createItemId(permission, namespace, repoName)
  };
}

export function deletePermissionFailure(
  permission: Permission,
  namespace: string,
  repoName?: string,
  error: Error
): Action {
  return {
    type: DELETE_PERMISSION_FAILURE,
    payload: {
      error,
      permission
    },
    itemId: createItemId(permission, namespace, repoName)
  };
}

export function deletePermissionReset(namespace: string, repoName?: string) {
  return {
    type: DELETE_PERMISSION_RESET,
    payload: {
      namespace,
      repoName
    },
    itemId: createPermissionStateKey(namespace, repoName)
  };
}

function deletePermissionFromState(oldPermissions: PermissionCollection, permission: Permission) {
  const newPermission = [];
  for (let i = 0; i < oldPermissions.length; i++) {
    if (
      oldPermissions[i].name !== permission.name ||
      oldPermissions[i].groupPermission !== permission.groupPermission
    ) {
      newPermission.push(oldPermissions[i]);
    }
  }
  return newPermission;
}

function createItemId(permission: Permission, namespace: string, repoName?: string) {
  const groupPermission = permission.groupPermission ? "@" : "";
  return createPermissionStateKey(namespace, repoName) + "/" + groupPermission + permission.name;
}

// reducer
export default function reducer(
  state: object = {},
  action: Action = {
    type: "UNKNOWN"
  }
): object {
  if (!action.payload) {
    return state;
  }
  switch (action.type) {
    case FETCH_AVAILABLE_SUCCESS:
      return {
        ...state,
        available: action.payload
      };
    case FETCH_PERMISSIONS_SUCCESS:
      return {
        ...state,
        [action.itemId]: {
          entries: action.payload._embedded.permissions,
          createPermission: !!action.payload._links.create
        }
      };
    case MODIFY_PERMISSION_SUCCESS: {
      const positionOfPermission = action.payload.position;
      const newPermission = newPermissions(state[action.payload.position].entries, action.payload.permission);
      return {
        ...state,
        [positionOfPermission]: {
          ...state[positionOfPermission],
          entries: newPermission
        }
      };
    }
    case CREATE_PERMISSION_SUCCESS: {
      // return state;
      const position = action.payload.position;
      const permissions = state[action.payload.position].entries;
      permissions.push(action.payload.permission);
      return {
        ...state,
        [position]: {
          ...state[position],
          entries: permissions
        }
      };
    }
    case DELETE_PERMISSION_SUCCESS: {
      const permissionPosition = action.payload.position;
      const newPermissions = deletePermissionFromState(
        state[action.payload.position].entries,
        action.payload.permission
      );
      return {
        ...state,
        [permissionPosition]: {
          ...state[permissionPosition],
          entries: newPermissions
        }
      };
    }
    default:
      return state;
  }
}

// selectors

export function getAvailablePermissions(state: object) {
  if (state.permissions) {
    return state.permissions.available;
  }
}

export function getAvailableRepositoryRoles(state: object) {
  return available(state).repositoryRoles;
}

export function getAvailableRepositoryVerbs(state: object) {
  return available(state).repositoryVerbs;
}

function available(state: object) {
  if (state.permissions && state.permissions.available) {
    return state.permissions.available;
  }
  return {};
}

export function getPermissionsOfRepo(state: object, namespace: string, repoName?: string) {
  if (state.permissions && state.permissions[createPermissionStateKey(namespace, repoName)]) {
    return state.permissions[createPermissionStateKey(namespace, repoName)].entries;
  }
}

export function isFetchAvailablePermissionsPending(state: object) {
  return isPending(state, FETCH_AVAILABLE, "available");
}

export function isFetchPermissionsPending(state: object, namespace: string, repoName?: string) {
  return isPending(state, FETCH_PERMISSIONS, createPermissionStateKey(namespace, repoName));
}

export function getFetchAvailablePermissionsFailure(state: object) {
  return getFailure(state, FETCH_AVAILABLE, "available");
}

export function getFetchPermissionsFailure(state: object, namespace: string, repoName?: string) {
  return getFailure(state, FETCH_PERMISSIONS, createPermissionStateKey(namespace, repoName));
}

export function isModifyPermissionPending(state: object, namespace: string, repoName?: string, permission: Permission) {
  return isPending(state, MODIFY_PERMISSION, createItemId(permission, createPermissionStateKey(namespace, repoName)));
}

export function getModifyPermissionFailure(
  state: object,
  namespace: string,
  repoName?: string,
  permission: Permission
) {
  return getFailure(state, MODIFY_PERMISSION, createItemId(permission, createPermissionStateKey(namespace, repoName)));
}

export function hasCreatePermission(state: object, namespace: string, repoName?: string) {
  if (state.permissions && state.permissions[createPermissionStateKey(namespace, repoName)])
    return state.permissions[createPermissionStateKey(namespace, repoName)].createPermission;
  else return null;
}

export function isCreatePermissionPending(state: object, namespace: string, repoName?: string) {
  return isPending(state, CREATE_PERMISSION, createPermissionStateKey(namespace, repoName));
}

export function getCreatePermissionFailure(state: object, namespace: string, repoName?: string) {
  return getFailure(state, CREATE_PERMISSION, createPermissionStateKey(namespace, repoName));
}

export function isDeletePermissionPending(state: object, namespace: string, repoName?: string, permission: Permission) {
  return isPending(state, DELETE_PERMISSION, createItemId(permission, namespace, repoName));
}

export function getDeletePermissionFailure(
  state: object,
  namespace: string,
  repoName?: string,
  permission: Permission
) {
  return getFailure(state, DELETE_PERMISSION, createItemId(permission, namespace, repoName));
}

export function getDeletePermissionsFailure(state: object, namespace: string, repoName?: string) {
  const permissions =
    state.permissions && state.permissions[createPermissionStateKey(namespace, repoName)]
      ? state.permissions[createPermissionStateKey(namespace, repoName)].entries
      : null;
  if (permissions == null) return undefined;
  for (let i = 0; i < permissions.length; i++) {
    if (getDeletePermissionFailure(state, namespace, repoName, permissions[i])) {
      return getFailure(state, DELETE_PERMISSION, createItemId(permissions[i], namespace, repoName));
    }
  }
  return null;
}

export function getModifyPermissionsFailure(state: object, namespace: string, repoName?: string) {
  const permissions =
    state.permissions && state.permissions[createPermissionStateKey(namespace, repoName)]
      ? state.permissions[createPermissionStateKey(namespace, repoName)].entries
      : null;
  if (permissions == null) return undefined;
  for (let i = 0; i < permissions.length; i++) {
    if (getModifyPermissionFailure(state, namespace, repoName, permissions[i])) {
      return getFailure(state, MODIFY_PERMISSION, createItemId(permissions[i], namespace, repoName));
    }
  }
  return null;
}

function createPermissionStateKey(namespace: string, repoName?: string) {
  return namespace + (repoName ? "/" + repoName : "");
}

export function findVerbsForRole(availableRepositoryRoles: RepositoryRole[], roleName: string) {
  const matchingRole = availableRepositoryRoles.find(role => roleName === role.name);
  if (matchingRole) {
    return matchingRole.verbs;
  } else {
    return [];
  }
}
