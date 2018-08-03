// @flow
import { apiClient } from "../../apiclient";
import { isPending } from "../../modules/pending";
import { getFailure } from "../../modules/failure";
import * as types from "../../modules/types";
import type { User } from "../types/User";
import { combineReducers, Dispatch } from "redux";
import type { Action } from "../../types/Action";
import type { PagedCollection } from "../../types/Collection";

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

export const DELETE_USER = "scm/users/DELETE";
export const DELETE_USER_PENDING = `${DELETE_USER}_${types.PENDING_SUFFIX}`;
export const DELETE_USER_SUCCESS = `${DELETE_USER}_${types.SUCCESS_SUFFIX}`;
export const DELETE_USER_FAILURE = `${DELETE_USER}_${types.FAILURE_SUFFIX}`;

const USERS_URL = "users";

const CONTENT_TYPE_USER = "application/vnd.scmm-user+json;v=2";

// TODO i18n for error messages

// fetch users

export function fetchUsers() {
  return fetchUsersByLink(USERS_URL);
}

export function fetchUsersByPage(page: number) {
  // backend start counting by 0
  return fetchUsersByLink(USERS_URL + "?page=" + (page - 1));
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
      .catch(cause => {
        const error = new Error(`could not fetch users: ${cause.message}`);
        dispatch(fetchUsersFailure(USERS_URL, error));
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
export function fetchUser(name: string) {
  const userUrl = USERS_URL + "/" + name;
  return function(dispatch: any) {
    dispatch(fetchUserPending(name));
    return apiClient
      .get(userUrl)
      .then(response => {
        return response.json();
      })
      .then(data => {
        dispatch(fetchUserSuccess(data));
      })
      .catch(cause => {
        const error = new Error(`could not fetch user: ${cause.message}`);
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

export function createUser(user: User, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(createUserPending(user));
    return apiClient
      .post(USERS_URL, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(createUserSuccess());
        if (callback) {
          callback();
        }
      })
      .catch(err =>
        dispatch(
          createUserFailure(
            new Error(`failed to add user ${user.name}: ${err.message}`)
          )
        )
      );
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
      .catch(cause => {
        const error = new Error(
          `could not delete user ${user.name}: ${cause.message}`
        );
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

function extractUsersByNames(
  users: User[],
  userNames: string[],
  oldUsersByNames: Object
) {
  const usersByNames = {};

  for (let user of users) {
    usersByNames[user.name] = user;
  }

  for (let userName in oldUsersByNames) {
    usersByNames[userName] = oldUsersByNames[userName];
  }
  return usersByNames;
}

function deleteUserInUsersByNames(users: {}, userName: string) {
  let newUsers = {};
  for (let username in users) {
    if (username !== userName) newUsers[username] = users[username];
  }
  return newUsers;
}

function deleteUserInEntries(users: [], userName: string) {
  let newUsers = [];
  for (let user of users) {
    if (user !== userName) newUsers.push(user);
  }
  return newUsers;
}

const reducerByName = (state: any, username: string, newUserState: any) => {
  const newUsersByNames = {
    ...state,
    [username]: newUserState
  };

  return newUsersByNames;
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
          userCreatePermission: action.payload._links.create ? true : false,
          page: action.payload.page,
          pageTotal: action.payload.pageTotal,
          _links: action.payload._links
        }
      };

    // Delete single user actions
    case DELETE_USER_SUCCESS:
      const newUserEntries = deleteUserInEntries(
        state.entries,
        action.payload.name
      );
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

    case MODIFY_USER_SUCCESS:
      return reducerByName(state, action.payload.name, action.payload);

    case DELETE_USER_SUCCESS:
      const newUserByNames = deleteUserInUsersByNames(
        state,
        action.payload.name
      );
      return newUserByNames;

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
  if (state.users && state.users.list) {
    return state.users.list;
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

export const isPermittedToCreateUsers = (state: Object): boolean => {
  const permission = selectListEntry(state).userCreatePermission;
  if (permission) {
    return true;
  }
  return false;
};

export function getUsersFromState(state: Object) {
  const userNames = selectList(state).entries;
  if (!userNames) {
    return null;
  }
  const userEntries: User[] = [];

  for (let userName of userNames) {
    userEntries.push(state.users.byNames[userName]);
  }

  return userEntries;
}

export function isFetchUsersPending(state: Object) {
  return isPending(state, FETCH_USERS);
}

export function getFetchUsersFailure(state: Object) {
  return getFailure(state, FETCH_USERS);
}

export function isCreateUserPending(state: Object) {
  return isPending(state, CREATE_USER);
}

export function getCreateUserFailure(state: Object) {
  return getFailure(state, CREATE_USER);
}

export function getUserByName(state: Object, name: string) {
  if (state.users && state.users.byNames) {
    return state.users.byNames[name];
  }
}

export function isFetchUserPending(state: Object, name: string) {
  return isPending(state, FETCH_USER, name);
}

export function getFetchUserFailure(state: Object, name: string) {
  return getFailure(state, FETCH_USER, name);
}

export function isModifyUserPending(state: Object, name: string) {
  return isPending(state, MODIFY_USER, name);
}

export function getModifyUserFailure(state: Object, name: string) {
  return getFailure(state, MODIFY_USER, name);
}

export function isDeleteUserPending(state: Object, name: string) {
  return isPending(state, DELETE_USER, name);
}

export function getDeleteUserFailure(state: Object, name: string) {
  return getFailure(state, DELETE_USER, name);
}
