// @flow
import { apiClient } from "../../apiclient";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";
import { Dispatch } from "redux";

export const FETCH_USERS_PENDING = "scm/users/FETCH_USERS_PENDING";
export const FETCH_USERS_SUCCESS = "scm/users/FETCH_USERS_SUCCESS";
export const FETCH_USERS_FAILURE = "scm/users/FETCH_USERS_FAILURE";

export const FETCH_USER_PENDING = "scm/users/FETCH_USER_PENDING";
export const FETCH_USER_SUCCESS = "scm/users/FETCH_USER_SUCCESS";
export const FETCH_USER_FAILURE = "scm/users/FETCH_USER_FAILURE";

export const CREATE_USER_PENDING = "scm/users/CREATE_USER_PENDING";
export const CREATE_USER_SUCCESS = "scm/users/CREATE_USER_SUCCESS";
export const CREATE_USER_FAILURE = "scm/users/CREATE_USER_FAILURE";

export const MODIFY_USER_PENDING = "scm/users/MODIFY_USER_PENDING";
export const MODIFY_USER_SUCCESS = "scm/users/MODIFY_USER_SUCCESS";
export const MODIFY_USER_FAILURE = "scm/users/MODIFY_USER_FAILURE";

export const DELETE_USER = "scm/users/DELETE";
export const DELETE_USER_SUCCESS = "scm/users/DELETE_SUCCESS";
export const DELETE_USER_FAILURE = "scm/users/DELETE_FAILURE";

const USERS_URL = "users";

const CONTENT_TYPE_USER = "application/vnd.scmm-user+json;v=2";

//fetch users

export function fetchUsers() {
  return function(dispatch: any) {
    dispatch(fetchUsersPending());
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
        dispatch(fetchUsersFailure(USERS_URL, error));
      });
  };
}

export function fetchUsersPending() {
  return {
    type: FETCH_USERS_PENDING
  };
}

export function fetchUsersSuccess(users: any) {
  return {
    type: FETCH_USERS_SUCCESS,
    payload: users
  };
}

export function fetchUsersFailure(url: string, error: Error) {
  return {
    type: FETCH_USERS_FAILURE,
    payload: {
      error,
      url
    }
  };
}

//fetch user
//TODO: fetchUsersPending and FetchUsersFailure are the wrong functions here!
export function fetchUser(name: string) {
  const userUrl = USERS_URL + "/" + name;
  return function(dispatch: any) {
    dispatch(fetchUsersPending());
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
        dispatch(fetchUsersFailure(USERS_URL, error));
      });
  };
}

export function fetchUserPending(name: string) {
  return {
    type: FETCH_USER_PENDING,
    payload: { name }
  };
}

export function fetchUserSuccess(user: any) {
  return {
    type: FETCH_USER_SUCCESS,
    payload: user
  };
}

export function fetchUserFailure(user: User, error: Error) {
  return {
    type: FETCH_USER_FAILURE,
    user,
    error
  };
}

//create user

export function createUser(user: User) {
  return function(dispatch: Dispatch) {
    dispatch(createUserPending(user));
    return apiClient
      .postWithContentType(USERS_URL, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(createUserSuccess());
        dispatch(fetchUsers());
      })
      .catch(err =>
        dispatch(
          createUserFailure(
            user,
            new Error(`failed to add user ${user.name}: ${err.message}`)
          )
        )
      );
  };
}

export function createUserPending(user: User) {
  return {
    type: CREATE_USER_PENDING,
    user
  };
}

export function createUserSuccess() {
  return {
    type: CREATE_USER_SUCCESS
  };
}

export function createUserFailure(user: User, err: Error) {
  return {
    type: CREATE_USER_FAILURE,
    payload: err,
    user
  };
}

//modify user

export function modifyUser(user: User) {
  return function(dispatch: Dispatch) {
    dispatch(modifyUserPending(user));
    return apiClient
      .putWithContentType(user._links.update.href, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(modifyUserSuccess(user));
        dispatch(fetchUsers());
      })
      .catch(err => {
        dispatch(modifyUserFailure(user, err));
      });
  };
}

function modifyUserPending(user: User) {
  return {
    type: MODIFY_USER_PENDING,
    user
  };
}

function modifyUserSuccess(user: User) {
  return {
    type: MODIFY_USER_SUCCESS,
    user
  };
}

export function modifyUserFailure(user: User, error: Error) {
  return {
    type: MODIFY_USER_FAILURE,
    payload: error,
    user
  };
}

//delete user

export function deleteUser(user: User) {
  return function(dispatch: any) {
    dispatch(deleteUserPending(user));
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

export function deleteUserPending(user: User) {
  return {
    type: DELETE_USER,
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
    payload: {
      error,
      user
    }
  };
}

//helper functions

export function getUsersFromState(state: any) {
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
    if (username !== userName) newUsers[username] = users[username];
  }
  return newUsers;
}

function deleteUserInEntries(users: [], userName: any) {
  let newUsers = [];
  for (let user of users) {
    if (user !== userName) newUsers.push(user);
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
      return reduceUsersByNames(state, action.user.name, {
        loading: true,
        error: action.error
      });
    // Delete single user cases
    case DELETE_USER:
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
    case CREATE_USER_PENDING:
      return {
        ...state,
        users: {
          ...state.users,
          loading: true,
          error: null
        }
      };
    case CREATE_USER_SUCCESS:
      return {
        ...state,
        users: {
          ...state.users,
          loading: false,
          error: null
        }
      };
    case CREATE_USER_FAILURE:
      return {
        ...state,
        users: {
          ...state.users,
          loading: false,
          error: action.payload
        }
      };
    // Update single user cases
    case MODIFY_USER_PENDING:
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
    case MODIFY_USER_SUCCESS:
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
