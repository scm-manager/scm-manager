// @flow
import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import * as types from "../../modules/types";
import { combineReducers, Dispatch } from "redux";
import type { Action, PagedCollection, Role } from "@scm-manager/ui-types";

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

export function fetchRolesByPage(link: string, page: number, filter?: string) {
  // backend start counting by 0
  if (filter) {
    return fetchRolesByLink(
      `${link}?page=${page - 1}&q=${decodeURIComponent(filter)}`
    );
  }
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

export function fetchRoleByLink(role: Role) {
  return fetchRole(role._links.self.href, role.name);
}

// create role
export function createRolePending(role: Role): Action {
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

export function createRole(link: string, role: Role, callback?: () => void) {
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
    default:
      return state;
  }
}

function extractRolesByNames(
  roles: Role[],
  roleNames: string[],
  oldRolesByNames: Object
) {
  const rolesByNames = {};

  for (let role of roles) {
    rolesByNames[role.name] = role;
  }

  for (let roleName in oldRolesByNames) {
    rolesByNames[roleName] = oldRolesByNames[roleName];
  }
  return rolesByNames;
}

const reducerByName = (state: any, rolename: string, newRoleState: any) => {
  return {
    ...state,
    [rolename]: newRoleState
  };
};

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
    default:
      return state;
  }
}

export default combineReducers({
  list: listReducer,
  byNames: byNamesReducer
});

// selectors
const selectList = (state: Object) => {
  if (state.repositoryRoles && state.repositoryRoles.list) {
    return state.repositoryRoles.list;
  }
  return {};
};

const selectListEntry = (state: Object): Object => {
  const list = selectList(state);
  if (list.entry) {
    return list.entry;
  }
  return {};
};

export const selectListAsCollection = (state: Object): PagedCollection => {
  return selectListEntry(state);
};

export const isPermittedToCreateRoles = (state: Object): boolean => {
  return !!selectListEntry(state).roleCreatePermission;
};

export function getRolesFromState(state: Object) {
  const roleNames = selectList(state).entries;
  if (!roleNames) {
    return null;
  }
  const roleEntries: Role[] = [];

  for (let roleName of roleNames) {
    roleEntries.push(state.repositoryRoles.byNames[roleName]);
  }

  return roleEntries;
}

export function isFetchRolesPending(state: Object) {
  return isPending(state, FETCH_ROLES);
}

export function getFetchRolesFailure(state: Object) {
  return getFailure(state, FETCH_ROLES);
}

export function isCreateRolePending(state: Object) {
  return isPending(state, CREATE_ROLE);
}

export function getCreateRoleFailure(state: Object) {
  return getFailure(state, CREATE_ROLE);
}

export function getRoleByName(state: Object, name: string) {
  if (state.repositoryRoles && state.repositoryRoles.byNames) {
    return state.repositoryRoles.byNames[name];
  }
}

export function isFetchRolePending(state: Object, name: string) {
  return isPending(state, FETCH_ROLE, name);
}

export function getFetchRoleFailure(state: Object, name: string) {
  return getFailure(state, FETCH_ROLE, name);
}
