// @flow
import { apiClient } from "../../apiclient";
import * as types from "../../modules/types";
import type { Action } from "../../types/Action";
import type { Permission, Permissions } from "../types/Permissions";

export const FETCH_PERMISSIONS = "scm/repos/FETCH_PERMISSIONS";
export const FETCH_PERMISSIONS_PENDING = `${FETCH_PERMISSIONS}_${types.PENDING_SUFFIX}`;
export const FETCH_PERMISSIONS_SUCCESS = `${FETCH_PERMISSIONS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_PERMISSIONS_FAILURE = `${FETCH_PERMISSIONS}_${types.FAILURE_SUFFIX}`;


const REPOS_URL = "repositories";
const PERMISSIONS_URL = "permissions";

// fetch repos

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

export function fetchPermissionsPending(namespace: string, name: string): Action {
  return {
    type: FETCH_PERMISSIONS_PENDING,
    payload: {
      namespace,
      name
    },
    itemId: namespace + "/" + name
  };
}

export function fetchPermissionsSuccess(permissions: Permissions, namespace: string, name: string): Action {
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
