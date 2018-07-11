//@flow

import { apiClient, NOT_AUTHENTICATED_ERROR } from "../apiclient";
import { fetchMe } from "./me";

const LOGIN_URL = "/auth/access_token";

export const LOGIN_REQUEST = "scm/auth/login_request";
export const LOGIN_SUCCESSFUL = "scm/auth/login_successful";
export const LOGIN_FAILED = "scm/auth/login_failed";

export function login(username: string, password: string) {
  const login_data = {
    cookie: true,
    grant_type: "password",
    username,
    password
  };
  return function(dispatch: any => void) {
    dispatch(loginRequest());
    return apiClient
      .post(LOGIN_URL, login_data)
      .then(response => {
        // not the best way or?
        dispatch(fetchMe());
        dispatch(loginSuccessful());
      })
      .catch(err => {
        dispatch(loginFailed(err));
      });
  };
}

export function loginRequest() {
  return {
    type: LOGIN_REQUEST
  };
}

export function loginSuccessful() {
  return {
    type: LOGIN_SUCCESSFUL
  };
}

export function loginFailed(error: Error) {
  return {
    type: LOGIN_FAILED,
    payload: error
  };
}

export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case LOGIN_REQUEST:
      return {
        ...state,
        loading: true,
        login: false,
        error: null
      };
    case LOGIN_SUCCESSFUL:
      return {
        ...state,
        loading: false,
        login: true,
        error: null
      };
    case LOGIN_FAILED:
      return {
        ...state,
        loading: false,
        login: false,
        error: action.payload
      };

    default:
      return state;
  }
}
