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
import { Action, PagedCollection, User } from "@scm-manager/ui-types";

export const FETCH_USERS = "scm/users/FETCH_USERS";
export const FETCH_USERS_PENDING = `${FETCH_USERS}_${types.PENDING_SUFFIX}`;
export const FETCH_USERS_SUCCESS = `${FETCH_USERS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_USERS_FAILURE = `${FETCH_USERS}_${types.FAILURE_SUFFIX}`;

export const FETCH_USER = "scm/users/FETCH_USER";
export const FETCH_USER_PENDING = `${FETCH_USER}_${types.PENDING_SUFFIX}`;
export const FETCH_USER_SUCCESS = `${FETCH_USER}_${types.SUCCESS_SUFFIX}`;
export const FETCH_USER_FAILURE = `${FETCH_USER}_${types.FAILURE_SUFFIX}`;

export const CREATE_USER = "scm/users/CREATE_USER";
export const CREATE_USER_PENDING = `${CREATE_USER}_${types.PENDING_SUFFIX}`;
export const CREATE_USER_SUCCESS = `${CREATE_USER}_${types.SUCCESS_SUFFIX}`;
export const CREATE_USER_FAILURE = `${CREATE_USER}_${types.FAILURE_SUFFIX}`;
export const CREATE_USER_RESET = `${CREATE_USER}_${types.RESET_SUFFIX}`;

export const MODIFY_USER = "scm/users/MODIFY_USER";
export const MODIFY_USER_PENDING = `${MODIFY_USER}_${types.PENDING_SUFFIX}`;
export const MODIFY_USER_SUCCESS = `${MODIFY_USER}_${types.SUCCESS_SUFFIX}`;
export const MODIFY_USER_FAILURE = `${MODIFY_USER}_${types.FAILURE_SUFFIX}`;
export const MODIFY_USER_RESET = `${MODIFY_USER}_${types.RESET_SUFFIX}`;

export const DELETE_USER = "scm/users/DELETE_USER";
export const DELETE_USER_PENDING = `${DELETE_USER}_${types.PENDING_SUFFIX}`;
export const DELETE_USER_SUCCESS = `${DELETE_USER}_${types.SUCCESS_SUFFIX}`;
export const DELETE_USER_FAILURE = `${DELETE_USER}_${types.FAILURE_SUFFIX}`;

export const CONTENT_TYPE_USER = "application/vnd.scmm-user+json;v=2";

// TODO i18n for error messages

// fetch users

export function fetchUsers(link: string) {
  return fetchUsersByLink(link);
}

export function fetchUsersByPage(link: string, page: number, filter?: string) {
  // backend start counting by 0
  if (filter) {
    return fetchUsersByLink(`${link}?page=${page - 1}&q=${decodeURIComponent(filter)}`);
  }
  return fetchUsersByLink(`${link}?page=${page - 1}`);
}

export function fetchUsersByLink(link: string) {
  return function(dispatch: any) {
    dispatch(fetchUsersPending());
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(data => {
        dispatch(fetchUsersSuccess(data));
      })
      .catch(error => {
        dispatch(fetchUsersFailure(link, error));
      });
  };
}

export function fetchUsersPending(): Action {
  return {
    type: FETCH_USERS_PENDING
  };
}

export function fetchUsersSuccess(users: any): Action {
  return {
    type: FETCH_USERS_SUCCESS,
    payload: users
  };
}

export function fetchUsersFailure(url: string, error: Error): Action {
  return {
    type: FETCH_USERS_FAILURE,
    payload: {
      error,
      url
    }
  };
}

//fetch user
export function fetchUserByName(link: string, name: string) {
  const userUrl = link.endsWith("/") ? link + name : link + "/" + name;
  return fetchUser(userUrl, name);
}

export function fetchUserByLink(user: User) {
  return fetchUser(user._links.self.href, user.name);
}

function fetchUser(link: string, name: string) {
  return function(dispatch: any) {
    dispatch(fetchUserPending(name));
    return apiClient
      .get(link)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchUserSuccess(data));
      })
      .catch(error => {
        dispatch(fetchUserFailure(name, error));
      });
  };
}

export function fetchUserPending(name: string): Action {
  return {
    type: FETCH_USER_PENDING,
    payload: name,
    itemId: name
  };
}

export function fetchUserSuccess(user: any): Action {
  return {
    type: FETCH_USER_SUCCESS,
    payload: user,
    itemId: user.name
  };
}

export function fetchUserFailure(name: string, error: Error): Action {
  return {
    type: FETCH_USER_FAILURE,
    payload: {
      name,
      error
    },
    itemId: name
  };
}

//create user

export function createUser(link: string, user: User, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(createUserPending(user));
    return apiClient
      .post(link, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(createUserSuccess());
        if (callback) {
          callback();
        }
      })
      .catch(error => dispatch(createUserFailure(error)));
  };
}

export function createUserPending(user: User): Action {
  return {
    type: CREATE_USER_PENDING,
    user
  };
}

export function createUserSuccess(): Action {
  return {
    type: CREATE_USER_SUCCESS
  };
}

export function createUserFailure(error: Error): Action {
  return {
    type: CREATE_USER_FAILURE,
    payload: error
  };
}

export function createUserReset() {
  return {
    type: CREATE_USER_RESET
  };
}

//modify user

export function modifyUser(user: User, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(modifyUserPending(user));
    return apiClient
      .put(user._links.update.href, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(modifyUserSuccess(user));
        if (callback) {
          callback();
        }
      })
      .then(() => {
        dispatch(fetchUserByLink(user));
      })
      .catch(err => {
        dispatch(modifyUserFailure(user, err));
      });
  };
}

export function modifyUserPending(user: User): Action {
  return {
    type: MODIFY_USER_PENDING,
    payload: user,
    itemId: user.name
  };
}

export function modifyUserSuccess(user: User): Action {
  return {
    type: MODIFY_USER_SUCCESS,
    payload: user,
    itemId: user.name
  };
}

export function modifyUserFailure(user: User, error: Error): Action {
  return {
    type: MODIFY_USER_FAILURE,
    payload: {
      error,
      user
    },
    itemId: user.name
  };
}

export function modifyUserReset(user: User): Action {
  return {
    type: MODIFY_USER_RESET,
    itemId: user.name
  };
}

//delete user

export function deleteUser(user: User, callback?: () => void) {
  return function(dispatch: any) {
    dispatch(deleteUserPending(user));
    return apiClient
      .delete(user._links.delete.href)
      .then(() => {
        dispatch(deleteUserSuccess(user));
        if (callback) {
          callback();
        }
      })
      .catch(error => {
        dispatch(deleteUserFailure(user, error));
      });
  };
}

export function deleteUserPending(user: User): Action {
  return {
    type: DELETE_USER_PENDING,
    payload: user,
    itemId: user.name
  };
}

export function deleteUserSuccess(user: User): Action {
  return {
    type: DELETE_USER_SUCCESS,
    payload: user,
    itemId: user.name
  };
}

export function deleteUserFailure(user: User, error: Error): Action {
  return {
    type: DELETE_USER_FAILURE,
    payload: {
      error,
      user
    },
    itemId: user.name
  };
}

function extractUsersByNames(users: User[], userNames: string[], oldUsersByNames: object) {
  const usersByNames = {};

  for (const user of users) {
    usersByNames[user.name] = user;
  }

  for (const userName in oldUsersByNames) {
    usersByNames[userName] = oldUsersByNames[userName];
  }
  return usersByNames;
}

function deleteUserInUsersByNames(users: {}, userName: string) {
  const newUsers = {};
  for (const username in users) {
    if (username !== userName) newUsers[username] = users[username];
  }
  return newUsers;
}

function deleteUserInEntries(users: [], userName: string) {
  const newUsers = [];
  for (const user of users) {
    if (user !== userName) newUsers.push(user);
  }
  return newUsers;
}

const reducerByName = (state: any, username: string, newUserState: any) => {
  return {
    ...state,
    [username]: newUserState
  };
};

function listReducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_USERS_SUCCESS:
      const users = action.payload._embedded.users;
      const userNames = users.map(user => user.name);
      return {
        ...state,
        entries: userNames,
        entry: {
          userCreatePermission: !!action.payload._links.create,
          page: action.payload.page,
          pageTotal: action.payload.pageTotal,
          _links: action.payload._links
        }
      };

    // Delete single user actions
    case DELETE_USER_SUCCESS:
      const newUserEntries = deleteUserInEntries(state.entries, action.payload.name);
      return {
        ...state,
        entries: newUserEntries
      };
    default:
      return state;
  }
}

function byNamesReducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    // Fetch all users actions
    case FETCH_USERS_SUCCESS:
      const users = action.payload._embedded.users;
      const userNames = users.map(user => user.name);
      const byNames = extractUsersByNames(users, userNames, state.byNames);
      return {
        ...byNames
      };

    // Fetch single user actions
    case FETCH_USER_SUCCESS:
      return reducerByName(state, action.payload.name, action.payload);

    case DELETE_USER_SUCCESS:
      return deleteUserInUsersByNames(state, action.payload.name);

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
  if (state.users && state.users.list) {
    return state.users.list;
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

export const isPermittedToCreateUsers = (state: object): boolean => {
  return !!selectListEntry(state).userCreatePermission;
};

export function getUsersFromState(state: object) {
  const userNames = selectList(state).entries;
  if (!userNames) {
    return null;
  }
  const userEntries: User[] = [];

  for (const userName of userNames) {
    userEntries.push(state.users.byNames[userName]);
  }

  return userEntries;
}

export function isFetchUsersPending(state: object) {
  return isPending(state, FETCH_USERS);
}

export function getFetchUsersFailure(state: object) {
  return getFailure(state, FETCH_USERS);
}

export function isCreateUserPending(state: object) {
  return isPending(state, CREATE_USER);
}

export function getCreateUserFailure(state: object) {
  return getFailure(state, CREATE_USER);
}

export function getUserByName(state: object, name: string) {
  if (state.users && state.users.byNames) {
    return state.users.byNames[name];
  }
}

export function isFetchUserPending(state: object, name: string) {
  return isPending(state, FETCH_USER, name);
}

export function getFetchUserFailure(state: object, name: string) {
  return getFailure(state, FETCH_USER, name);
}

export function isModifyUserPending(state: object, name: string) {
  return isPending(state, MODIFY_USER, name);
}

export function getModifyUserFailure(state: object, name: string) {
  return getFailure(state, MODIFY_USER, name);
}

export function isDeleteUserPending(state: object, name: string) {
  return isPending(state, DELETE_USER, name);
}

export function getDeleteUserFailure(state: object, name: string) {
  return getFailure(state, DELETE_USER, name);
}
