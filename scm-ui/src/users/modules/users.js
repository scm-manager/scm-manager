// @flow
import { apiClient, PAGE_NOT_FOUND_ERROR } from "../../apiclient";
import type { User } from "../types/User";
import { ThunkDispatch } from "redux-thunk";

const FETCH_USERS = "scm/users/FETCH";
const FETCH_USERS_SUCCESS = "scm/users/FETCH_SUCCESS";
const FETCH_USERS_FAILURE = "scm/users/FETCH_FAILURE";
const FETCH_USERS_NOTFOUND = "scm/users/FETCH_NOTFOUND";

const ADD_USER = "scm/users/ADD";
const ADD_USER_SUCCESS = "scm/users/ADD_SUCCESS";
const ADD_USER_FAILURE = "scm/users/ADD_FAILURE";

const EDIT_USER = "scm/users/EDIT";
const EDIT_USER_SUCCESS = "scm/users/EDIT_SUCCESS";
const EDIT_USER_FAILURE = "scm/users/EDIT_FAILURE";

const DELETE_USER = "scm/users/DELETE";
const DELETE_USER_SUCCESS = "scm/users/DELETE_SUCCESS";
const DELETE_USER_FAILURE = "scm/users/DELETE_FAILURE";

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
  return function(dispatch: ThunkDispatch) {
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
        if (err === PAGE_NOT_FOUND_ERROR) {
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
  return function(dispatch: ThunkDispatch) {
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

function requestAddUser(user: User) {
  return {
    type: ADD_USER,
    user
  };
}

export function editUser(user: User) {
  return function(dispatch: ThunkDispatch) {
    dispatch(requestAddUser(user));
    return apiClient
      .putWithContentType(USERS_URL + "/" + user.name, user, CONTENT_TYPE_USER)
      .then(() => {
        dispatch(addUserSuccess());
        dispatch(fetchUsers());
      })
      .catch(err => dispatch(addUserFailure(user, err)));
  };
}

function editUserSuccess() {
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

  return function(dispatch: ThunkDispatch) {
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

export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case FETCH_USERS:
    case DELETE_USER:
      return {
        ...state,
        users: null,
        loading: true
      };
    case FETCH_USERS_SUCCESS:
      return {
        ...state,
        error: null,
        users: action.payload._embedded.users,
        loading: false
      };
    case FETCH_USERS_FAILURE:
    case DELETE_USER_FAILURE:
      return {
        ...state,
        login: false,
        error: action.payload,
        loading: false
      };

    default:
      return state;
  }
}
