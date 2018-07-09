// @flow

const FETCH_USERS = "scm/users/FETCH";
const FETCH_USERS_SUCCESS = "scm/users/FETCH_SUCCESS";
const FETCH_USERS_FAILURE = "scm/users/FETCH_FAILURE";

function requestUsers() {
  return {
    type: FETCH_USERS
  };
}

function fetchUsers() {
  return function(dispatch) {
    dispatch(requestUsers());
    return null;
  };
}

export function shouldFetchUsers(state: any): boolean {
  const users = state.users;
  return null;
}

export function fetchUsersIfNeeded() {
  return (dispatch, getState) => {
    if (shouldFetchUsers(getState())) {
      dispatch(fetchUsers());
    }
  };
}

export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_USERS:
      return {
        ...state,
        login: true,
        error: null
      };
    case FETCH_USERS_SUCCESS:
      return {
        ...state,
        login: false,
        timestamp: action.timestamp,
        error: null,
        users: action.payload
      };
    case FETCH_USERS_FAILURE:
      return {
        ...state,
        login: false,
        error: action.payload
      };

    default:
      return state;
  }
}
