// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type { PermissionCollection } from "../types/Permissions";
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

const REPOS_URL = "repositories";
const PERMISSIONS_URL = "permissions";

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
  permissions: PermissionCollection,
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
        [action.itemId]: action.payload
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
