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

import { apiClient } from "@scm-manager/ui-components";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import * as types from "../../modules/types";
import { combineReducers, Dispatch } from "redux";
import { Action, Group, PagedCollection } from "@scm-manager/ui-types";

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
export const MODIFY_GROUP_RESET = `${MODIFY_GROUP}_${types.RESET_SUFFIX}`;

export const DELETE_GROUP = "scm/groups/DELETE_GROUP";
export const DELETE_GROUP_PENDING = `${DELETE_GROUP}_${types.PENDING_SUFFIX}`;
export const DELETE_GROUP_SUCCESS = `${DELETE_GROUP}_${types.SUCCESS_SUFFIX}`;
export const DELETE_GROUP_FAILURE = `${DELETE_GROUP}_${types.FAILURE_SUFFIX}`;

const CONTENT_TYPE_GROUP = "application/vnd.scmm-group+json;v=2";

// fetch groups
export function fetchGroups(link: string) {
  return fetchGroupsByLink(link);
}

export function fetchGroupsByPage(link: string, page: number, filter?: string) {
  // backend start counting by 0
  if (filter) {
    return fetchGroupsByLink(`${link}?page=${page - 1}&q=${decodeURIComponent(filter)}`);
  }
  return fetchGroupsByLink(`${link}?page=${page - 1}`);
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
      .catch(error => {
        dispatch(fetchGroupsFailure(link, error));
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
export function fetchGroupByLink(group: Group) {
  return fetchGroup(group._links.self.href, group.name);
}

export function fetchGroupByName(link: string, name: string) {
  const groupUrl = link.endsWith("/") ? link + name : link + "/" + name;
  return fetchGroup(groupUrl, name);
}

function fetchGroup(link: string, name: string) {
  return function(dispatch: any) {
    dispatch(fetchGroupPending(name));
    return apiClient
      .get(link)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchGroupSuccess(data));
      })
      .catch(error => {
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
export function createGroup(link: string, group: Group, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(createGroupPending());
    return apiClient
      .post(link, group, CONTENT_TYPE_GROUP)
      .then(() => {
        dispatch(createGroupSuccess());
        if (callback) {
          callback();
        }
      })
      .catch(error => {
        dispatch(createGroupFailure(error));
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
  };
}

// modify group
export function modifyGroup(group: Group, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(modifyGroupPending(group));
    return apiClient
      .put(group._links.update.href, group, CONTENT_TYPE_GROUP)
      .then(() => {
        dispatch(modifyGroupSuccess(group));
        if (callback) {
          callback();
        }
      })
      .then(() => {
        dispatch(fetchGroupByLink(group));
      })
      .catch(error => {
        dispatch(modifyGroupFailure(group, error));
      });
  };
}

export function modifyGroupPending(group: Group): Action {
  return {
    type: MODIFY_GROUP_PENDING,
    payload: group,
    itemId: group.name
  };
}

export function modifyGroupSuccess(group: Group): Action {
  return {
    type: MODIFY_GROUP_SUCCESS,
    payload: group,
    itemId: group.name
  };
}

export function modifyGroupFailure(group: Group, error: Error): Action {
  return {
    type: MODIFY_GROUP_FAILURE,
    payload: {
      error,
      group
    },
    itemId: group.name
  };
}

export function modifyGroupReset(group: Group): Action {
  return {
    type: MODIFY_GROUP_RESET,
    itemId: group.name
  };
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
      .catch(error => {
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
function extractGroupsByNames(groups: Group[], groupNames: string[], oldGroupsByNames: object) {
  const groupsByNames = {};

  for (const group of groups) {
    groupsByNames[group.name] = group;
  }

  for (const groupName in oldGroupsByNames) {
    groupsByNames[groupName] = oldGroupsByNames[groupName];
  }
  return groupsByNames;
}

function deleteGroupInGroupsByNames(groups: {}, groupName: string) {
  const newGroups = {};
  for (const groupname in groups) {
    if (groupname !== groupName) newGroups[groupname] = groups[groupname];
  }
  return newGroups;
}

function deleteGroupInEntries(groups: [], groupName: string) {
  const newGroups = [];
  for (const group of groups) {
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
          groupCreatePermission: !!action.payload._links.create,
          page: action.payload.page,
          pageTotal: action.payload.pageTotal,
          _links: action.payload._links
        }
      };
    // Delete single group actions
    case DELETE_GROUP_SUCCESS:
      const newGroupEntries = deleteGroupInEntries(state.entries, action.payload.name);
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
      const newGroupByNames = deleteGroupInGroupsByNames(state, action.payload.name);
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

const selectList = (state: object) => {
  if (state.groups && state.groups.list) {
    return state.groups.list;
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

export const isPermittedToCreateGroups = (state: object): boolean => {
  const permission = selectListEntry(state).groupCreatePermission;
  if (permission) {
    return true;
  }
  return false;
};

export function getCreateGroupLink(state: object) {
  if (state.groups.list.entry && state.groups.list.entry._links) return state.groups.list.entry._links.create.href;
  return undefined;
}

export function getGroupsFromState(state: object) {
  const groupNames = selectList(state).entries;
  if (!groupNames) {
    return null;
  }
  const groupEntries: Group[] = [];

  for (const groupName of groupNames) {
    groupEntries.push(state.groups.byNames[groupName]);
  }

  return groupEntries;
}

export function isFetchGroupsPending(state: object) {
  return isPending(state, FETCH_GROUPS);
}

export function getFetchGroupsFailure(state: object) {
  return getFailure(state, FETCH_GROUPS);
}

export function isCreateGroupPending(state: object) {
  return isPending(state, CREATE_GROUP);
}

export function getCreateGroupFailure(state: object) {
  return getFailure(state, CREATE_GROUP);
}

export function isModifyGroupPending(state: object, name: string) {
  return isPending(state, MODIFY_GROUP, name);
}

export function getModifyGroupFailure(state: object, name: string) {
  return getFailure(state, MODIFY_GROUP, name);
}

export function getGroupByName(state: object, name: string) {
  if (state.groups && state.groups.byNames) {
    return state.groups.byNames[name];
  }
}

export function isFetchGroupPending(state: object, name: string) {
  return isPending(state, FETCH_GROUP, name);
}

export function getFetchGroupFailure(state: object, name: string) {
  return getFailure(state, FETCH_GROUP, name);
}

export function isDeleteGroupPending(state: object, name: string) {
  return isPending(state, DELETE_GROUP, name);
}

export function getDeleteGroupFailure(state: object, name: string) {
  return getFailure(state, DELETE_GROUP, name);
}
