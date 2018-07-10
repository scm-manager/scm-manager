// @flow

const FETCH_USERS = "scm/users/FETCH";
const FETCH_USERS_SUCCESS = "scm/users/FETCH_SUCCESS";
const FETCH_USERS_FAILURE = "scm/users/FETCH_FAILURE";

const USERS_URL = "/scm/api/rest/v2/users";

function requestUsers() {
  return {
    type: FETCH_USERS
  };
}

export function fetchUsers() {
  return function(dispatch) {
    // dispatch(requestUsers());
    return fetch(USERS_URL, {
      credentials: "same-origin",
      headers: {
        Cache: "no-cache"
      }
    })
      .then(response => {
        if (response.ok) {
          return response.json();
        }
      })
      .then(data => {
        dispatch(fetchUsersSuccess(data));
      });
  };
}

function fetchUsersSuccess(users: any) {
  return {
    type: FETCH_USERS_SUCCESS,
    payload: users
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
        users: [{ name: "" }]
      };
    case FETCH_USERS_SUCCESS:
      return {
        ...state,
        timestamp: action.timestamp,
        error: null,
        users: action.payload._embedded.users
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
