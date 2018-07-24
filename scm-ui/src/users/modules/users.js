// @flow
import { apiClient, NOT_FOUND_ERROR } from "../../apiclient";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";
import asyncActionCreator from "./asyncActionCreator";

export const FETCH_USERS_PENDING = "scm/users/FETCH_PENDING";
export const FETCH_USERS_SUCCESS = "scm/users/FETCH_SUCCESS";
export const FETCH_USERS_FAILURE = "scm/users/FETCH_FAILURE";
export const FETCH_USERS_NOTFOUND = "scm/users/FETCH_NOTFOUND";

export const FETCH_USER_PENDING = "scm/users/FETCH_USER_PENDING";
export const FETCH_USER_SUCCESS = "scm/users/FETCH_USER_SUCCESS";
export const FETCH_USER_FAILURE = "scm/users/FETCH_USER_FAILURE";

export const ADD_USER_PENDING = "scm/users/ADD_PENDING";
export const ADD_USER_SUCCESS = "scm/users/ADD_SUCCESS";
export const ADD_USER_FAILURE = "scm/users/ADD_FAILURE";

export const UPDATE_USER_PENDING = "scm/users/UPDATE_PENDING";
export const UPDATE_USER_SUCCESS = "scm/users/UPDATE_SUCCESS";
export const UPDATE_USER_FAILURE = "scm/users/UPDATE_FAILURE";

export const DELETE_USER_PENDING = "scm/users/DELETE_PENDING";
export const DELETE_USER_SUCCESS = "scm/users/DELETE_SUCCESS";
export const DELETE_USER_FAILURE = "scm/users/DELETE_FAILURE";

const USERS_URL = "users";
const USER_URL = "users/";

const CONTENT_TYPE_USER = "application/vnd.scmm-user+json;v=2";

export function requestUsers() {
  return {
    type: FETCH_USERS_PENDING
  };
}

export function failedToFetchUsers(url: string, error: Error) {
  return {
    type: FETCH_USERS_FAILURE,
    payload: {
      error,
      url
    }
  };
}

export function fetchUsers() {
  return function(dispatch: any) {
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
      .catch(cause => {
        const error = new Error(`could not fetch users: ${cause.message}`);
        dispatch(failedToFetchUsers(USERS_URL, error));
      });
  };
}

export function fetchUsersSuccess(users: any) {
  return {
    type: FETCH_USERS_SUCCESS,
    payload: users
  };
}

export function requestUser(name: string) {
  return {
    type: FETCH_USER_PENDING,
    payload: { name }
  };
}

export function fetchUser(name: string) {
  const userUrl = USER_URL + name;
  return function(dispatch: any) {
    dispatch(requestUser(name));
    return apiClient
      .get(userUrl)
      .then(response => {
        return response;
      })
      .then(response => {
        if (response.ok) {
          return response.json();
        }
      })
      .then(data => {
        dispatch(fetchUserSuccess(data));
      })
      .catch(cause => {
        const error = new Error(`could not fetch user: ${cause.message}`);
        dispatch(fetchUserFailure(USERS_URL, error));
      });
  };
}

export function fetchUserSuccess(user: User) {
  return {
    type: FETCH_USER_SUCCESS,
    payload: user
  };
}

export function fetchUserFailure(username: string, error: Error) {
  return {
    type: FETCH_USER_FAILURE,
    error: true,
    payload: {
      username,
      error
    }
  };
}

export function requestAddUser(user: User) {
  return {
    type: ADD_USER_PENDING,
    payload: user
  };
}

export function addUser(user: User) {
  return function(dispatch: Dispatch) {
    dispatch(requestAddUser(user));
    return apiClient
      .postWithContentType(USERS_URL, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(addUserSuccess());
        dispatch(fetchUsers());
      })
      .catch(err =>
        dispatch(
          addUserFailure(
            user,
            new Error(`failed to add user ${user.name}: ${err.message}`)
          )
        )
      );
  };
}

export function addUserSuccess() {
  return {
    type: ADD_USER_SUCCESS
  };
}

export function addUserFailure(user: User, error: Error) {
  return {
    type: ADD_USER_FAILURE,
    error: true,
    payload: {
      error,
      user
    }
  };
}

function requestUpdateUser(user: User) {
  return {
    type: UPDATE_USER_PENDING,
    payload: user
  };
}

export function updateUser(user: User) {
  return function(dispatch: Dispatch) {
    dispatch(requestUpdateUser(user));
    return apiClient
      .putWithContentType(user._links.update.href, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(updateUserSuccess(user));
        dispatch(fetchUsers());
      })
      .catch(err => {
        console.log(err);
        dispatch(updateUserFailure(user, err));
      });
  };
}

function updateUserSuccess(user: User) {
  return {
    type: UPDATE_USER_SUCCESS,
    payload: user
  };
}

export function updateUserFailure(user: User, error: Error) {
  return {
    type: UPDATE_USER_FAILURE,
    error: true,
    payload: {
      error,
      user
    }
  };
}

export function requestDeleteUser(user: User) {
  return {
    type: DELETE_USER_PENDING,
    payload: user
  };
}

export function deleteUserSuccess(user: User) {
  return {
    type: DELETE_USER_SUCCESS,
    payload: user
  };
}

export function deleteUserFailure(user: User, error: Error) {
  return {
    type: DELETE_USER_FAILURE,
    error: true,
    payload: {
      error,
      user
    }
  };
}

export function deleteUser(user: User) {
  return function(dispatch: any) {
    dispatch(requestDeleteUser(user));
    return apiClient
      .delete(user._links.delete.href)
      .then(() => {
        dispatch(deleteUserSuccess(user));
        dispatch(fetchUsers());
      })
      .catch(cause => {
        const error = new Error(
          `could not delete user ${user.name}: ${cause.message}`
        );
        dispatch(deleteUserFailure(user, error));
      });
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
  const userEntries: Array<UserEntry> = [];

  for (let userName of userNames) {
    userEntries.push(state.users.usersByNames[userName]);
  }

  return userEntries;
}

function extractUsersByNames(
  users: Array<User>,
  userNames: Array<string>,
  oldUsersByNames: {}
) {
  const usersByNames = {};

  for (let user of users) {
    usersByNames[user.name] = {
      entry: user
    };
  }

  for (let userName in oldUsersByNames) {
    usersByNames[userName] = oldUsersByNames[userName];
  }
  return usersByNames;
}
function deleteUserInUsersByNames(users: {}, userName: any) {
  let newUsers = {};
  for (let username in users) {
    if (username != userName) newUsers[username] = users[username];
  }
  return newUsers;
}

function deleteUserInEntries(users: [], userName: any) {
  let newUsers = [];
  for (let user of users) {
    if (user != userName) newUsers.push(user);
  }
  return newUsers;
}

const reduceUsersByNames = (
  state: any,
  username: string,
  newUserState: any
) => {
  const newUsersByNames = {
    ...state.usersByNames,
    [username]: newUserState
  };

  return {
    ...state,
    usersByNames: newUsersByNames
  };
};

export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    // fetch user list cases
    case FETCH_USERS_PENDING:
      return {
        ...state,
        users: {
          loading: true
        }
      };
    case FETCH_USERS_SUCCESS:
      // return red(state, action.payload._embedded.users);
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
          userCreatePermission: action.payload._links.create ? true : false,
          error: null,
          entries: userNames,
          loading: false
        },
        usersByNames
      };
    case FETCH_USERS_FAILURE:
      return {
        ...state,
        users: {
          ...state.users,
          loading: false,
          error: action.payload.error
        }
      };
    // Fetch single user cases
    case FETCH_USER_PENDING:
      return reduceUsersByNames(state, action.payload.name, {
        loading: true,
        error: null
      });

    case FETCH_USER_SUCCESS:
      const ubn = extractUsersByNames(
        [action.payload],
        [action.payload.name],
        state.usersByNames
      );
      return {
        ...state,
        users: {
          error: null,
          entries: [action.payload.name],
          loading: false
        },
        usersByNames: ubn
      };
    case FETCH_USER_FAILURE:
      return reduceUsersByNames(state, action.payload.username, {
        loading: true,
        error: action.payload.error
      });
    // Delete single user cases
    case DELETE_USER_PENDING:
      return reduceUsersByNames(state, action.payload.name, {
        loading: true,
        error: null,
        entry: action.payload
      });

    case DELETE_USER_SUCCESS:
      const newUserByNames = deleteUserInUsersByNames(state.usersByNames, [
        action.payload.name
      ]);
      const newUserEntries = deleteUserInEntries(state.users.entries, [
        action.payload.name
      ]);
      return {
        ...state,
        users: {
          ...state.users,
          entries: newUserEntries
        },
        usersByNames: newUserByNames
      };

    case DELETE_USER_FAILURE:
      const newState = reduceUsersByNames(state, action.payload.user.name, {
        loading: false,
        error: action.payload.error,
        entry: action.payload.user
      });
      return {
        ...newState,
        users: {
          ...newState.users,
          error: action.payload.error
        }
      };
    // Add single user cases
    case ADD_USER_PENDING:
      return {
        ...state,
        users: {
          ...state.users,
          loading: true,
          error: null
        }
      };
    case ADD_USER_SUCCESS:
      return {
        ...state,
        users: {
          ...state.users,
          loading: false,
          error: null
        }
      };
    case ADD_USER_FAILURE:
      return {
        ...state,
        users: {
          ...state.users,
          loading: false,
          error: action.payload.error
        }
      };
    // Update single user cases
    case UPDATE_USER_PENDING:
      return {
        ...state,
        usersByNames: {
          ...state.usersByNames,
          [action.user.name]: {
            loading: true,
            error: null,
            entry: action.user
          }
        }
      };
    case UPDATE_USER_SUCCESS:
      return {
        ...state,
        usersByNames: {
          ...state.usersByNames,
          [action.user.name]: {
            loading: false,
            error: null,
            entry: action.user
          }
        }
      };
    default:
      return state;
  }
}
