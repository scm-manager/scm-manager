///
/// MIT License
///
/// Copyright (c) 2020-present Cloudogu GmbH and Contributors
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
///

import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../../modules/pending";
import { getFailure } from "../../../modules/failure";
import * as types from "../../../modules/types";
import { combineReducers, Dispatch } from "redux";
import { Action, PagedCollection, RepositoryRole } from "@scm-manager/ui-types";

export const FETCH_ROLES = "scm/roles/FETCH_ROLES";
export const FETCH_ROLES_PENDING = `${FETCH_ROLES}_${types.PENDING_SUFFIX}`;
export const FETCH_ROLES_SUCCESS = `${FETCH_ROLES}_${types.SUCCESS_SUFFIX}`;
export const FETCH_ROLES_FAILURE = `${FETCH_ROLES}_${types.FAILURE_SUFFIX}`;

export const FETCH_ROLE = "scm/roles/FETCH_ROLE";
export const FETCH_ROLE_PENDING = `${FETCH_ROLE}_${types.PENDING_SUFFIX}`;
export const FETCH_ROLE_SUCCESS = `${FETCH_ROLE}_${types.SUCCESS_SUFFIX}`;
export const FETCH_ROLE_FAILURE = `${FETCH_ROLE}_${types.FAILURE_SUFFIX}`;

export const CREATE_ROLE = "scm/roles/CREATE_ROLE";
export const CREATE_ROLE_PENDING = `${CREATE_ROLE}_${types.PENDING_SUFFIX}`;
export const CREATE_ROLE_SUCCESS = `${CREATE_ROLE}_${types.SUCCESS_SUFFIX}`;
export const CREATE_ROLE_FAILURE = `${CREATE_ROLE}_${types.FAILURE_SUFFIX}`;
export const CREATE_ROLE_RESET = `${CREATE_ROLE}_${types.RESET_SUFFIX}`;

export const MODIFY_ROLE = "scm/roles/MODIFY_ROLE";
export const MODIFY_ROLE_PENDING = `${MODIFY_ROLE}_${types.PENDING_SUFFIX}`;
export const MODIFY_ROLE_SUCCESS = `${MODIFY_ROLE}_${types.SUCCESS_SUFFIX}`;
export const MODIFY_ROLE_FAILURE = `${MODIFY_ROLE}_${types.FAILURE_SUFFIX}`;
export const MODIFY_ROLE_RESET = `${MODIFY_ROLE}_${types.RESET_SUFFIX}`;

export const DELETE_ROLE = "scm/roles/DELETE_ROLE";
export const DELETE_ROLE_PENDING = `${DELETE_ROLE}_${types.PENDING_SUFFIX}`;
export const DELETE_ROLE_SUCCESS = `${DELETE_ROLE}_${types.SUCCESS_SUFFIX}`;
export const DELETE_ROLE_FAILURE = `${DELETE_ROLE}_${types.FAILURE_SUFFIX}`;

export const FETCH_VERBS = "scm/roles/FETCH_VERBS";
export const FETCH_VERBS_PENDING = `${FETCH_VERBS}_${types.PENDING_SUFFIX}`;
export const FETCH_VERBS_SUCCESS = `${FETCH_VERBS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_VERBS_FAILURE = `${FETCH_VERBS}_${types.FAILURE_SUFFIX}`;

const CONTENT_TYPE_ROLE = "application/vnd.scmm-repositoryRole+json;v=2";

// fetch roles
export function fetchRolesPending(): Action {
  return {
    type: FETCH_ROLES_PENDING
  };
}

export function fetchRolesSuccess(roles: any): Action {
  return {
    type: FETCH_ROLES_SUCCESS,
    payload: roles
  };
}

export function fetchRolesFailure(url: string, error: Error): Action {
  return {
    type: FETCH_ROLES_FAILURE,
    payload: {
      error,
      url
    }
  };
}

export function fetchRolesByLink(link: string) {
  return function(dispatch: any) {
    dispatch(fetchRolesPending());
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(data => {
        dispatch(fetchRolesSuccess(data));
      })
      .catch(error => {
        dispatch(fetchRolesFailure(link, error));
      });
  };
}

export function fetchRoles(link: string) {
  return fetchRolesByLink(link);
}

export function fetchRolesByPage(link: string, page: number) {
  // backend start counting by 0
  return fetchRolesByLink(`${link}?page=${page - 1}`);
}

// fetch role
export function fetchRolePending(name: string): Action {
  return {
    type: FETCH_ROLE_PENDING,
    payload: name,
    itemId: name
  };
}

export function fetchRoleSuccess(role: any): Action {
  return {
    type: FETCH_ROLE_SUCCESS,
    payload: role,
    itemId: role.name
  };
}

export function fetchRoleFailure(name: string, error: Error): Action {
  return {
    type: FETCH_ROLE_FAILURE,
    payload: {
      name,
      error
    },
    itemId: name
  };
}

function fetchRole(link: string, name: string) {
  return function(dispatch: any) {
    dispatch(fetchRolePending(name));
    return apiClient
      .get(link)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchRoleSuccess(data));
      })
      .catch(error => {
        dispatch(fetchRoleFailure(name, error));
      });
  };
}

export function fetchRoleByName(link: string, name: string) {
  const roleUrl = link.endsWith("/") ? link + name : link + "/" + name;
  return fetchRole(roleUrl, name);
}

export function fetchRoleByLink(role: RepositoryRole) {
  return fetchRole(role._links.self.href, role.name);
}

// create role
export function createRolePending(role: RepositoryRole): Action {
  return {
    type: CREATE_ROLE_PENDING,
    role
  };
}

export function createRoleSuccess(): Action {
  return {
    type: CREATE_ROLE_SUCCESS
  };
}

export function createRoleFailure(error: Error): Action {
  return {
    type: CREATE_ROLE_FAILURE,
    payload: error
  };
}

export function createRoleReset() {
  return {
    type: CREATE_ROLE_RESET
  };
}

export function createRole(link: string, role: RepositoryRole, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(createRolePending(role));
    return apiClient
      .post(link, role, CONTENT_TYPE_ROLE)
      .then(() => {
        dispatch(createRoleSuccess());
        if (callback) {
          callback();
        }
      })
      .catch(error => dispatch(createRoleFailure(error)));
  };
}

//fetch verbs
export function fetchVerbsPending(): Action {
  return {
    type: FETCH_VERBS_PENDING
  };
}

export function fetchVerbsSuccess(verbs: any): Action {
  return {
    type: FETCH_VERBS_SUCCESS,
    payload: verbs
  };
}

export function fetchVerbsFailure(error: Error): Action {
  return {
    type: FETCH_VERBS_FAILURE,
    payload: error
  };
}

export function fetchAvailableVerbs(link: string) {
  return function(dispatch: any) {
    dispatch(fetchVerbsPending());
    return apiClient
      .get(link)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchVerbsSuccess(data));
      })
      .catch(error => {
        dispatch(fetchVerbsFailure(error));
      });
  };
}

function verbReducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_VERBS_SUCCESS:
      const verbs = action.payload.verbs;
      return {
        ...state,
        verbs
      };
    default:
      return state;
  }
}

// modify role
export function modifyRolePending(role: RepositoryRole): Action {
  return {
    type: MODIFY_ROLE_PENDING,
    payload: role,
    itemId: role.name
  };
}

export function modifyRoleSuccess(role: RepositoryRole): Action {
  return {
    type: MODIFY_ROLE_SUCCESS,
    payload: role,
    itemId: role.name
  };
}

export function modifyRoleFailure(role: RepositoryRole, error: Error): Action {
  return {
    type: MODIFY_ROLE_FAILURE,
    payload: {
      error,
      role
    },
    itemId: role.name
  };
}

export function modifyRoleReset(role: RepositoryRole): Action {
  return {
    type: MODIFY_ROLE_RESET,
    itemId: role.name
  };
}

export function modifyRole(role: RepositoryRole, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(modifyRolePending(role));
    return apiClient
      .put(role._links.update.href, role, CONTENT_TYPE_ROLE)
      .then(() => {
        dispatch(modifyRoleSuccess(role));
        if (callback) {
          callback();
        }
      })
      .then(() => {
        dispatch(fetchRoleByLink(role));
      })
      .catch(err => {
        dispatch(modifyRoleFailure(role, err));
      });
  };
}

// delete role
export function deleteRolePending(role: RepositoryRole): Action {
  return {
    type: DELETE_ROLE_PENDING,
    payload: role,
    itemId: role.name
  };
}

export function deleteRoleSuccess(role: RepositoryRole): Action {
  return {
    type: DELETE_ROLE_SUCCESS,
    payload: role,
    itemId: role.name
  };
}

export function deleteRoleFailure(role: RepositoryRole, error: Error): Action {
  return {
    type: DELETE_ROLE_FAILURE,
    payload: {
      error,
      role
    },
    itemId: role.name
  };
}

export function deleteRole(role: RepositoryRole, callback?: () => void) {
  return function(dispatch: any) {
    dispatch(deleteRolePending(role));
    return apiClient
      .delete(role._links.delete.href)
      .then(() => {
        dispatch(deleteRoleSuccess(role));
        if (callback) {
          callback();
        }
      })
      .catch(error => {
        dispatch(deleteRoleFailure(role, error));
      });
  };
}

function extractRolesByNames(roles: RepositoryRole[], roleNames: string[], oldRolesByNames: object) {
  const rolesByNames = {};

  for (const role of roles) {
    rolesByNames[role.name] = role;
  }

  for (const roleName in oldRolesByNames) {
    rolesByNames[roleName] = oldRolesByNames[roleName];
  }
  return rolesByNames;
}

function deleteRoleInRolesByNames(roles: {}, roleName: string) {
  const newRoles = {};
  for (const rolename in roles) {
    if (rolename !== roleName) newRoles[rolename] = roles[rolename];
  }
  return newRoles;
}

function deleteRoleInEntries(roles: [], roleName: string) {
  const newRoles = [];
  for (const role of roles) {
    if (role !== roleName) newRoles.push(role);
  }
  return newRoles;
}

const reducerByName = (state: any, rolename: string, newRoleState: any) => {
  return {
    ...state,
    [rolename]: newRoleState
  };
};

function listReducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_ROLES_SUCCESS:
      const roles = action.payload._embedded.repositoryRoles;
      const roleNames = roles.map(role => role.name);
      return {
        ...state,
        entries: roleNames,
        entry: {
          roleCreatePermission: !!action.payload._links.create,
          page: action.payload.page,
          pageTotal: action.payload.pageTotal,
          _links: action.payload._links
        }
      };

    // Delete single role actions
    case DELETE_ROLE_SUCCESS:
      const newRoleEntries = deleteRoleInEntries(state.entries, action.payload.name);
      return {
        ...state,
        entries: newRoleEntries
      };
    default:
      return state;
  }
}

function byNamesReducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    // Fetch all roles actions
    case FETCH_ROLES_SUCCESS:
      const roles = action.payload._embedded.repositoryRoles;
      const roleNames = roles.map(role => role.name);
      const byNames = extractRolesByNames(roles, roleNames, state.byNames);
      return {
        ...byNames
      };

    // Fetch single role actions
    case FETCH_ROLE_SUCCESS:
      return reducerByName(state, action.payload.name, action.payload);

    case DELETE_ROLE_SUCCESS:
      return deleteRoleInRolesByNames(state, action.payload.name);

    default:
      return state;
  }
}

export default combineReducers({
  list: listReducer,
  byNames: byNamesReducer,
  verbs: verbReducer
});

// selectors
const selectList = (state: object) => {
  if (state.roles && state.roles.list) {
    return state.roles.list;
  }
  return {};
};
const selectListEntry = (state: object): object => {
  const list = selectList(state);
  if (list.entry) {
    return list.entry;
  }
  return {};
};

export const selectListAsCollection = (state: object): PagedCollection => {
  return selectListEntry(state);
};

export const isPermittedToCreateRoles = (state: object): boolean => {
  return !!selectListEntry(state).roleCreatePermission;
};

export function getRolesFromState(state: object) {
  const roleNames = selectList(state).entries;
  if (!roleNames) {
    return null;
  }
  const roleEntries: RepositoryRole[] = [];

  for (const roleName of roleNames) {
    roleEntries.push(state.roles.byNames[roleName]);
  }

  return roleEntries;
}

export function getRoleCreateLink(state: object) {
  if (state && state.list && state.list._links && state.list._links.create) {
    return state.list._links.create.href;
  }
}

export function getVerbsFromState(state: object) {
  return state.roles.verbs.verbs;
}

export function isFetchRolesPending(state: object) {
  return isPending(state, FETCH_ROLES);
}

export function getFetchRolesFailure(state: object) {
  return getFailure(state, FETCH_ROLES);
}

export function isFetchVerbsPending(state: object) {
  return isPending(state, FETCH_VERBS);
}

export function getFetchVerbsFailure(state: object) {
  return getFailure(state, FETCH_VERBS);
}

export function isCreateRolePending(state: object) {
  return isPending(state, CREATE_ROLE);
}

export function getCreateRoleFailure(state: object) {
  return getFailure(state, CREATE_ROLE);
}

export function getRoleByName(state: object, name: string) {
  if (state.roles && state.roles.byNames) {
    return state.roles.byNames[name];
  }
}

export function isFetchRolePending(state: object, name: string) {
  return isPending(state, FETCH_ROLE, name);
}

export function getFetchRoleFailure(state: object, name: string) {
  return getFailure(state, FETCH_ROLE, name);
}

export function isModifyRolePending(state: object, name: string) {
  return isPending(state, MODIFY_ROLE, name);
}

export function getModifyRoleFailure(state: object, name: string) {
  return getFailure(state, MODIFY_ROLE, name);
}

export function isDeleteRolePending(state: object, name: string) {
  return isPending(state, DELETE_ROLE, name);
}

export function getDeleteRoleFailure(state: object, name: string) {
  return getFailure(state, DELETE_ROLE, name);
}
