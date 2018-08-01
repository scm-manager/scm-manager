import { apiClient } from "../../apiclient";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import * as types from "../../modules/types";
import { combineReducers, Dispatch } from "redux";
import type { Action } from "../../types/Action";
import type { PagedCollection } from "../../types/Collection";
import type { Groups } from "../types/Groups";

export const FETCH_GROUPS = "scm/groups/FETCH_GROUPS";
export const FETCH_GROUPS_PENDING = `${FETCH_GROUPS}_${types.PENDING_SUFFIX}`;
export const FETCH_GROUPS_SUCCESS = `${FETCH_GROUPS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_GROUPS_FAILURE = `${FETCH_GROUPS}_${types.FAILURE_SUFFIX}`;

export const FETCH_GROUP = "scm/groups/FETCH_GROUP";
export const FETCH_GROUP_PENDING = `${FETCH_GROUP}_${types.PENDING_SUFFIX}`;
export const FETCH_GROUP_SUCCESS = `${FETCH_GROUP}_${types.SUCCESS_SUFFIX}`;
export const FETCH_GROUP_FAILURE = `${FETCH_GROUP}_${types.FAILURE_SUFFIX}`;

export const CREATE_GROUP = "scm/groups/CREATE_GROUP";
export const CREATE_GROUP_PENDING = `${CREATE_GROUP}_${types.PENDING_SUFFIX}`;
export const CREATE_GROUP_SUCCESS = `${CREATE_GROUP}_${types.SUCCESS_SUFFIX}`;
export const CREATE_GROUP_FAILURE = `${CREATE_GROUP}_${types.FAILURE_SUFFIX}`;
export const CREATE_GROUP_RESET = `${CREATE_GROUP}_${types.RESET_SUFFIX}`;

export const MODIFY_GROUP = "scm/groups/MODIFY_GROUP";
export const MODIFY_GROUP_PENDING = `${MODIFY_GROUP}_${types.PENDING_SUFFIX}`;
export const MODIFY_GROUP_SUCCESS = `${MODIFY_GROUP}_${types.SUCCESS_SUFFIX}`;
export const MODIFY_GROUP_FAILURE = `${MODIFY_GROUP}_${types.FAILURE_SUFFIX}`;

export const DELETE_GROUP = "scm/groups/DELETE";
export const DELETE_GROUP_PENDING = `${DELETE_GROUP}_${types.PENDING_SUFFIX}`;
export const DELETE_GROUP_SUCCESS = `${DELETE_GROUP}_${types.SUCCESS_SUFFIX}`;
export const DELETE_GROUP_FAILURE = `${DELETE_GROUP}_${types.FAILURE_SUFFIX}`;

const GROUPS_URL = "groups";
const CONTENT_TYPE_GROUP = "application/vnd.scmm-group+json;v=2";

// fetch groups
export function fetchGroups() {
  return fetchGroupsByLink(GROUPS_URL);
}

export function fetchGroupsByPage(page: number) {
  // backend start counting by 0
  return fetchGroupsByLink(GROUPS_URL + "?page=" + (page - 1));
}

export function fetchGroupsByLink(link: string) {
  return function(dispatch: any) {
    dispatch(fetchGroupsPending());
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(data => {
        dispatch(fetchGroupsSuccess(data));
      })
      .catch(cause => {
        const error = new Error(`could not fetch groups: ${cause.message}`);
        dispatch(fetchGroupsFailure(GROUPS_URL, error));
      });
  };
}

export function fetchGroupsPending(): Action {
  return {
    type: FETCH_GROUPS_PENDING
  };
}

export function fetchGroupsSuccess(groups: any): Action {
  return {
    type: FETCH_GROUPS_SUCCESS,
    payload: groups
  };
}

export function fetchGroupsFailure(url: string, error: Error): Action {
  return {
    type: FETCH_GROUPS_FAILURE,
    payload: {
      error,
      url
    }
  };
}

//fetch group
export function fetchGroup(name: string) {
  const groupUrl = GROUPS_URL + "/" + name;
  return function(dispatch: any) {
    dispatch(fetchGroupPending(name));
    return apiClient
      .get(groupUrl)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchGroupSuccess(data));
      })
      .catch(cause => {
        const error = new Error(`could not fetch group: ${cause.message}`);
        dispatch(fetchGroupFailure(name, error));
      });
  };
}

export function fetchGroupPending(name: string): Action {
  return {
    type: FETCH_GROUP_PENDING,
    payload: name,
    itemId: name
  };
}

export function fetchGroupSuccess(group: any): Action {
  return {
    type: FETCH_GROUP_SUCCESS,
    payload: group,
    itemId: group.name
  };
}

export function fetchGroupFailure(name: string, error: Error): Action {
  return {
    type: FETCH_GROUP_FAILURE,
    payload: {
      name,
      error
    },
    itemId: name
  };
}

//create group
export function createGroup(group: Group, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(createGroupPending());
    return apiClient
      .postWithContentType(GROUPS_URL, group, CONTENT_TYPE_GROUP)
      .then(() => {
        dispatch(createGroupSuccess())
      if (callback) {
        callback();
      }})
      .catch(error => {
        dispatch(
          createGroupFailure(
            new Error(`Failed to create group ${group.name}: ${error.message}`)
          )
        );
      });
  };
}

export function createGroupPending() {
  return {
    type: CREATE_GROUP_PENDING
  };
}

export function createGroupSuccess() {
  return {
    type: CREATE_GROUP_SUCCESS
  };
}

export function createGroupFailure(error: Error) {
  return {
    type: CREATE_GROUP_FAILURE,
    payload: error
  };
}

export function createGroupReset() {
  return {
    type: CREATE_GROUP_RESET
  }
}
//delete group

export function deleteGroup(group: Group, callback?: () => void) {
  return function(dispatch: any) {
    dispatch(deleteGroupPending(group));
    return apiClient
      .delete(group._links.delete.href)
      .then(() => {
        dispatch(deleteGroupSuccess(group));
        if (callback) {
          callback();
        }
      })
      .catch(cause => {
        const error = new Error(
          `could not delete group ${group.name}: ${cause.message}`
        );
        dispatch(deleteGroupFailure(group, error));
      });
  };
}

export function deleteGroupPending(group: Group): Action {
  return {
    type: DELETE_GROUP_PENDING,
    payload: group,
    itemId: group.name
  };
}

export function deleteGroupSuccess(group: Group): Action {
  return {
    type: DELETE_GROUP_SUCCESS,
    payload: group,
    itemId: group.name
  };
}

export function deleteGroupFailure(group: Group, error: Error): Action {
  return {
    type: DELETE_GROUP_FAILURE,
    payload: {
      error,
      group
    },
    itemId: group.name
  };
}

//reducer
function extractGroupsByNames(
  groups: Groups[],
  groupNames: string[],
  oldGroupsByNames: Object
) {
  const groupsByNames = {};

  for (let group of groups) {
    groupsByNames[group.name] = group;
  }

  for (let groupName in oldGroupsByNames) {
    groupsByNames[groupName] = oldGroupsByNames[groupName];
  }
  return groupsByNames;
}

function deleteGroupInGroupsByNames(groups: {}, groupName: string) {
  let newGroups = {};
  for (let groupname in groups) {
    if (groupname !== groupName) newGroups[groupname] = groups[groupname];
  }
  return newGroups;
}

function deleteGroupInEntries(groups: [], groupName: string) {
  let newGroups = [];
  for (let group of groups) {
    if (group !== groupName) newGroups.push(group);
  }
  return newGroups;
}

const reducerByName = (state: any, groupname: string, newGroupState: any) => {
  const newGroupsByNames = {
    ...state,
    [groupname]: newGroupState
  };

  return newGroupsByNames;
};

function listReducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_GROUPS_SUCCESS:
      const groups = action.payload._embedded.groups;
      const groupNames = groups.map(group => group.name);
      return {
        ...state,
        entries: groupNames,
        entry: {
          groupCreatePermission: action.payload._links.create ? true : false,
          page: action.payload.page,
          pageTotal: action.payload.pageTotal,
          _links: action.payload._links
        }
      };
    // Delete single group actions
    case DELETE_GROUP_SUCCESS:
    const newGroupEntries = deleteGroupInEntries(
      state.entries,
      action.payload.name
    );
    return {
      ...state,
      entries: newGroupEntries
    };
    default:
      return state;
  }
}

function byNamesReducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    // Fetch all groups actions
    case FETCH_GROUPS_SUCCESS:
      const groups = action.payload._embedded.groups;
      const groupNames = groups.map(group => group.name);
      const byNames = extractGroupsByNames(groups, groupNames, state.byNames);
      return {
        ...byNames
      };
    case FETCH_GROUP_SUCCESS:
      return reducerByName(state, action.payload.name, action.payload);
    case DELETE_GROUP_SUCCESS:
      const newGroupByNames = deleteGroupInGroupsByNames(
        state,
        action.payload.name
      );
      return newGroupByNames;

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
  if (state.groups && state.groups.list) {
    return state.groups.list;
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

export const isPermittedToCreateGroups = (state: Object): boolean => {
  const permission = selectListEntry(state).groupCreatePermission;
  if (permission) {
    return true;
  }
  return false;
};

export function getGroupsFromState(state: Object) {
  const groupNames = selectList(state).entries;
  if (!groupNames) {
    return null;
  }
  const groupEntries: Group[] = [];

  for (let groupName of groupNames) {
    groupEntries.push(state.groups.byNames[groupName]);
  }

  return groupEntries;
}

export function isFetchGroupsPending(state: Object) {
  return isPending(state, FETCH_GROUPS);
}

export function getFetchGroupsFailure(state: Object) {
  return getFailure(state, FETCH_GROUPS);
}

export function isCreateGroupPending(state: Object) {
  return isPending(state, CREATE_GROUP);
}

export function getCreateGroupFailure(state: Object) {
  return getFailure(state, CREATE_GROUP);
}

export function getGroupByName(state: Object, name: string) {
  if (state.groups && state.groups.byNames) {
    return state.groups.byNames[name];
  }
}

export function isFetchGroupPending(state: Object, name: string) {
  return isPending(state, FETCH_GROUP, name);
}

export function getFetchGroupFailure(state: Object, name: string) {
  return getFailure(state, FETCH_GROUP, name);
}

export function isDeleteGroupPending(state: Object, name: string) {
  return isPending(state, DELETE_GROUP, name);
}

export function getDeleteGroupFailure(state: Object, name: string) {
  return getFailure(state, DELETE_GROUP, name);
}
