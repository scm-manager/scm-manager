// @flow
import { apiClient } from "../../apiclient";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";
import { Dispatch } from "redux";
import type { Action } from "../../types/Action";
import type { PageCollectionStateSlice } from "../../types/Collection";

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
        dispatch(fetchUserFailure(name, error));
      });
  };
}

export function fetchUserPending(name: string): Action {
  return {
    type: FETCH_USER_PENDING,
    payload: { name }
  };
}

export function fetchUserSuccess(user: any): Action {
  return {
    type: FETCH_USER_SUCCESS,
    payload: user
  };
}

export function fetchUserFailure(username: string, error: Error): Action {
  return {
    type: FETCH_USER_FAILURE,
    payload: {
      username,
      error
    }
  };
}

//create user

export function createUser(user: User, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(createUserPending(user));
    return apiClient
      .postWithContentType(USERS_URL, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(createUserSuccess());
        if (callback) {
          callback();
        }
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

export function createUserFailure(user: User, err: Error): Action {
  return {
    type: CREATE_USER_FAILURE,
    payload: err,
    user
  };
}

//modify user

export function modifyUser(user: User, callback?: () => void) {
  return function(dispatch: Dispatch) {
    dispatch(modifyUserPending(user));
    return apiClient
      .putWithContentType(user._links.update.href, user, CONTENT_TYPE_USER)
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
    payload: user
  };
}

export function modifyUserSuccess(user: User): Action {
  return {
    type: MODIFY_USER_SUCCESS,
    payload: user
  };
}

export function modifyUserFailure(user: User, error: Error): Action {
  return {
    type: MODIFY_USER_FAILURE,
    payload: {
      error,
      user
    }
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
    type: DELETE_USER,
    payload: user
  };
}

export function deleteUserSuccess(user: User): Action {
  return {
    type: DELETE_USER_SUCCESS,
    payload: user
  };
}

export function deleteUserFailure(user: User, error: Error): Action {
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
  if (!state.users.list) {
    return null;
  }
  const userNames = state.users.list.entries;
  if (!userNames) {
    return null;
  }
  const userEntries: Array<UserEntry> = [];

  for (let userName of userNames) {
    userEntries.push(state.users.byNames[userName]);
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

const reducerByName = (state: any, username: string, newUserState: any) => {
  const newUsersByNames = {
    ...state.byNames,
    [username]: newUserState
  };

  return {
    ...state,
    byNames: newUsersByNames
  };
};

export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    // fetch user list cases
    case FETCH_USERS_PENDING:
      return {
        ...state,
        list: {
          loading: true
        }
      };
    case FETCH_USERS_SUCCESS:
      const users = action.payload._embedded.users;
      const userNames = users.map(user => user.name);
      const byNames = extractUsersByNames(users, userNames, state.byNames);
      return {
        ...state,
        list: {
          error: null,
          loading: false,
          entries: userNames,
          entry: {
            userCreatePermission: action.payload._links.create ? true : false,
            page: action.payload.page,
            pageTotal: action.payload.pageTotal,
            _links: action.payload._links
          }
        },
        byNames
      };
    case FETCH_USERS_FAILURE:
      return {
        ...state,
        list: {
          ...state.users,
          loading: false,
          error: action.payload.error
        }
      };
    // Fetch single user cases
    case FETCH_USER_PENDING:
      return reducerByName(state, action.payload.name, {
        loading: true,
        error: null
      });

    case FETCH_USER_SUCCESS:
      return reducerByName(state, action.payload.name, {
        loading: false,
        error: null,
        entry: action.payload
      });

    case FETCH_USER_FAILURE:
      return reducerByName(state, action.payload.username, {
        loading: false,
        error: action.payload.error
      });

    // Delete single user cases
    case DELETE_USER:
      return reducerByName(state, action.payload.name, {
        loading: true,
        error: null,
        entry: action.payload
      });

    case DELETE_USER_SUCCESS:
      const newUserByNames = deleteUserInUsersByNames(state.byNames, [
        action.payload.name
      ]);
      const newUserEntries = deleteUserInEntries(state.list.entries, [
        action.payload.name
      ]);
      return {
        ...state,
        list: {
          ...state.list,
          entries: newUserEntries
        },
        byNames: newUserByNames
      };

    case DELETE_USER_FAILURE:
      return reducerByName(state, action.payload.user.name, {
        loading: false,
        error: action.payload.error,
        entry: action.payload.user
      });

    // Add single user cases
    case CREATE_USER_PENDING:
      return {
        ...state,
        create: {
          loading: true
        }
      };
    case CREATE_USER_SUCCESS:
      return {
        ...state,
        create: {
          loading: false
        }
      };
    case CREATE_USER_FAILURE:
      return {
        ...state,
        create: {
          loading: false,
          error: action.payload
        }
      };

    // Update single user cases
    case MODIFY_USER_PENDING:
      return reducerByName(state, action.payload.name, {
        loading: true
      });
    case MODIFY_USER_SUCCESS:
      return reducerByName(state, action.payload.name, {
        entry: action.payload
      });
    case MODIFY_USER_FAILURE:
      return reducerByName(state, action.payload.user.name, {
        error: action.payload.error
      });
    default:
      return state;
  }
}

// selectors

const selectList = (state: Object) => {
  if (state.users && state.users.list) {
    return state.users.list;
  }
  return {};
};

const selectListEntry = (state: Object) => {
  const list = selectList(state);
  if (list.entry) {
    return list.entry;
  }
  return {};
};

export const selectListAsCollection = (
  state: Object
): PageCollectionStateSlice => {
  return selectList(state);
};

export const isPermittedToCreateUsers = (state: Object): boolean => {
  const permission = selectListEntry(state).userCreatePermission;
  if (permission) {
    return true;
  }
  return false;
};
