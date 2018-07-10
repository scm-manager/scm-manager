// @flow
import {apiClient, PAGE_NOT_FOUND_ERROR} from '../../apiclient';

const FETCH_USERS = "scm/users/FETCH";
const FETCH_USERS_SUCCESS = "scm/users/FETCH_SUCCESS";
const FETCH_USERS_FAILURE = "scm/users/FETCH_FAILURE";
const FETCH_USERS_NOTFOUND = 'scm/users/FETCH_NOTFOUND';

const DELETE_USER = "scm/users/DELETE";
const DELETE_USER_SUCCESS = "scm/users/DELETE_SUCCESS";
const DELETE_USER_FAILURE = "scm/users/DELETE_FAILURE";

const USERS_URL = "users";

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

  return function(dispatch) {
    dispatch(requestUsers());
    return apiClient.get(USERS_URL)
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
      .catch((err) => {
        if (err === PAGE_NOT_FOUND_ERROR) {
          dispatch(usersNotFound(USERS_URL));
        } else {
          dispatch(failedToFetchUsers(USERS_URL, err));
        }
      });
  }
}

function fetchUsersSuccess(users: any) {
  return {
    type: FETCH_USERS_SUCCESS,
    payload: users
  };
}

export function shouldFetchUsers(state: any): boolean {
  if(state.users.users == null){
    return true;
  }
  return false;
}

export function fetchUsersIfNeeded() {
  return (dispatch, getState) => {
    if (shouldFetchUsers(getState())) {
      dispatch(fetchUsers());
    }
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
    type: DELETE_USER_SUCCESS,
  };
}

function deleteUserFailure(url: string, err: Error) {
  return {
    type: DELETE_USER_FAILURE,
    payload: err,
    url
  };
}

export function deleteUser(username: string) {
  return function(dispatch) {
    dispatch(requestDeleteUser(username));
    return apiClient.delete(USERS_URL + '/' + username)
      .then(() => {
        dispatch(deleteUserSuccess());
      })
      .catch((err) => dispatch(deleteUserFailure(username, err)));
  }
}



export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_USERS:
      return {
        ...state,
        users: null
      };
    case FETCH_USERS_SUCCESS:
      return {
        ...state,
        error: null,
        users: action.payload._embedded.users
      };
    case FETCH_USERS_FAILURE:
      return {
        ...state,
        login: false,
        error: action.payload
      };
    case DELETE_USER_SUCCESS:
      return {
        ...state,
        users: null
      };

    default:
      return state;
  }
}
