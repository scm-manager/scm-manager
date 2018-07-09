//@flow

const LOGIN_URL = "/scm/api/rest/v2/auth/access_token";
const AUTHENTICATION_INFO_URL = "/scm/api/rest/v2/me";

export const LOGIN = "scm/auth/login";
export const LOGIN_REQUEST = "scm/auth/login_request";
export const LOGIN_SUCCESSFUL = "scm/auth/login_successful";
export const LOGIN_FAILED = "scm/auth/login_failed";
export const GET_IS_AUTHENTICATED_REQUEST = "scm/auth/is_authenticated_request";
export const GET_IS_AUTHENTICATED = "scm/auth/get_is_authenticated";
export const IS_AUTHENTICATED = "scm/auth/is_authenticated";
export const IS_NOT_AUTHENTICATED = "scm/auth/is_not_authenticated";

export function getIsAuthenticatedRequest() {
  return {
    type: GET_IS_AUTHENTICATED_REQUEST
  };
}

export function getIsAuthenticated() {
  return function(dispatch) {
    dispatch(getIsAuthenticatedRequest());

    return fetch(AUTHENTICATION_INFO_URL, {
      credentials: "same-origin",
      headers: {
        Cache: "no-cache"
      }
    })
      .then(response => {
        if (response.ok) {
          return response.json();
        } else {
          dispatch(isNotAuthenticated());
        }
      })
      .then(data => {
        if (data) {
          dispatch(isAuthenticated(data.username));
        }
      });
  };
}

export function isAuthenticated(username: string) {
  return {
    type: IS_AUTHENTICATED,
    username
  };
}

export function isNotAuthenticated() {
  return {
    type: IS_NOT_AUTHENTICATED
  };
}

export function loginRequest() {
  return {
    type: LOGIN_REQUEST
  };
}

export function login(username: string, password: string) {
  var login_data = {
    cookie: true,
    grant_type: "password",
    password: username,
    username: password
  };
  return function(dispatch) {
    dispatch(loginRequest());
    return fetch(LOGIN_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json; charset=utf-8"
      },
      credentials: "same-origin",
      body: JSON.stringify(login_data)
    }).then(
      response => {
        if (response.ok) {
          dispatch(getIsAuthenticated());
          dispatch(loginSuccessful());
        }
      },
      error => console.log("error logging in: " + error)
    );
  };
}

export function loginSuccessful() {
  return {
    type: LOGIN_SUCCESSFUL
  };
}

export default function reducer(state: any = {}, action: any = {}) {
  switch (action.type) {
    case LOGIN:
      return {
        ...state,
        login: false,
        error: null
      };
    case LOGIN_SUCCESSFUL:
      return {
        ...state,
        login: true,
        error: null
      };
    case LOGIN_FAILED:
      return {
        ...state,
        login: false,
        error: action.payload
      };
    case IS_AUTHENTICATED:
      return {
        ...state,
        login: true,
        username: action.username
      };
    case IS_NOT_AUTHENTICATED:
      return {
        ...state,
        login: false,
        username: null,
        error: null
      };

    default:
      return state;
  }
}
