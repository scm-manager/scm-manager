// @flow
import {
  apiClient,
  NOT_FOUND_ERROR
} from "../../apiclient";
import type {
  User
} from "../types/User";
import type {
  UserEntry
} from "../types/UserEntry";
import {
  Dispatch
} from "redux";

export const FETCH_USERS = "scm/users/FETCH";
export const FETCH_USERS_SUCCESS = "scm/users/FETCH_SUCCESS";
export const FETCH_USERS_FAILURE = "scm/users/FETCH_FAILURE";
export const FETCH_USERS_NOTFOUND = "scm/users/FETCH_NOTFOUND";

export const ADD_USER = "scm/users/ADD";
export const ADD_USER_SUCCESS = "scm/users/ADD_SUCCESS";
export const ADD_USER_FAILURE = "scm/users/ADD_FAILURE";

export const EDIT_USER = "scm/users/EDIT";

export const UPDATE_USER = "scm/users/UPDATE";
export const UPDATE_USER_SUCCESS = "scm/users/UPDATE_SUCCESS";
export const UPDATE_USER_FAILURE = "scm/users/UPDATE_FAILURE";

export const DELETE_USER = "scm/users/DELETE";
export const DELETE_USER_SUCCESS = "scm/users/DELETE_SUCCESS";
export const DELETE_USER_FAILURE = "scm/users/DELETE_FAILURE";

const USERS_URL = "users";

const CONTENT_TYPE_USER = "application/vnd.scmm-user+json;v=2";

function requestUsers() {
  return {
    type: FETCH_USERS
  };
}

function failedToFetchUsers(url: string, err: Error) {
  return {
    type: FETCH_USERS_FAILURE,
    payload: err,
    url
  };
}

function usersNotFound(url: string) {
  return {
    type: FETCH_USERS_NOTFOUND,
    url
  };
}

export function fetchUsers() {
  return function (dispatch: any) {
    dispatch(requestUsers());
    return apiClient
      .get(USERS_URL)
      .then(response => {
        return response;
      })
      .then(response => {
        if (response.ok) {
          return response.json();
        }
      })
      .then(data => {
        dispatch(fetchUsersSuccess(data));
      })
      .catch(err => {
        if (err === NOT_FOUND_ERROR) {
          dispatch(usersNotFound(USERS_URL));
        } else {
          dispatch(failedToFetchUsers(USERS_URL, err));
        }
      });
  };
}

function fetchUsersSuccess(users: any) {
  return {
    type: FETCH_USERS_SUCCESS,
    payload: users
  };
}

function requestAddUser(user: User) {
  return {
    type: ADD_USER,
    user
  };
}

export function addUser(user: User) {
  return function (dispatch: Dispatch) {
    dispatch(requestAddUser(user));
    return apiClient
      .postWithContentType(USERS_URL, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(addUserSuccess());
        dispatch(fetchUsers());
      })
      .catch(err => dispatch(addUserFailure(user, err)));
  };
}

function addUserSuccess() {
  return {
    type: ADD_USER_SUCCESS
  };
}

function addUserFailure(user: User, err: Error) {
  return {
    type: ADD_USER_FAILURE,
    payload: err,
    user
  };
}

function requestUpdateUser(user: User) {
  return {
    type: UPDATE_USER,
    user
  };
}

export function updateUser(user: User) {
  return function (dispatch: Dispatch) {
    dispatch(requestUpdateUser(user));
    return apiClient
      .putWithContentType(user._links.update.href, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(updateUserSuccess());
        dispatch(fetchUsers());
      })
      .catch(err => dispatch(updateUserFailure(user, err)));
  };
}

function updateUserSuccess() {
  return {
    type: UPDATE_USER_SUCCESS
  };
}

function updateUserFailure(user: User, error: Error) {
  return {
    type: UPDATE_USER_FAILURE,
    payload: error,
    user
  };
}

function requestDeleteUser(url: string) {
  return {
    type: DELETE_USER,
    url
  };
}

function deleteUserSuccess() {
  return {
    type: DELETE_USER_SUCCESS
  };
}

function deleteUserFailure(url: string, err: Error) {
  return {
    type: DELETE_USER_FAILURE,
    payload: err,
    url
  };
}

export function deleteUser(link: string) {
  return function (dispatch: ThunkDispatch) {
    dispatch(requestDeleteUser(link));
    return apiClient
      .delete(link)
      .then(() => {
        dispatch(deleteUserSuccess());
        dispatch(fetchUsers());
      })
      .catch(err => dispatch(deleteUserFailure(link, err)));
  };
}

export function getUsersFromState(state) {
  if (!state.users.users) {
    return null;
  }
  const userNames = state.users.users.entries;
  if (!userNames) {
    return null;
  }
  var userEntries: Array < UserEntry > = [];

  for (let userName of userNames) {
    userEntries.push(state.users.usersByNames[userName]);
  }

  return userEntries;
}

function extractUsersByNames(
  users: Array < User > ,
  userNames: Array < string > ,
  oldUsersByNames: {}
) {
  var usersByNames = {};

  for (let user of users) {
    usersByNames[user.name] = {
      entry: user
    };
  }

  for (var userName in oldUsersByNames) {
    usersByNames[userName] = oldUsersByNames[userName];
  }
  return usersByNames;
}

export function editUser(user: User) {
  return {
    type: EDIT_USER,
    user
  };
}

export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_USERS:
      return {
        ...state,
        users: {
          error: null,
          entries: null,
          loading: true
        }
      };
    case DELETE_USER:
      return {
        ...state,
        users: null
      };
    case FETCH_USERS_SUCCESS:
      const users = action.payload._embedded.users;
      const userNames = users.map(user => user.name);
      const usersByNames = extractUsersByNames(
        users,
        userNames,
        state.usersByNames
      );

      return {
        ...state,
        users: {
          error: null,
          entries: userNames,
          loading: false
        },
        usersByNames
      };
    case FETCH_USERS_FAILURE:
      return {
        ...state,
        error: action.payload,
        loading: false
      };
    case DELETE_USER_FAILURE:
      return {
        ...state,
        error: action.payload,
        loading: false
      };
    case EDIT_USER:
      return {
        ...state,
        editUser: action.user
      };
    default:
      return state;
  }
}
