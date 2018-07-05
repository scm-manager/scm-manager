//@flow

const LOGIN = "scm/auth/login";
const LOGIN_REQUEST = "scm/auth/login_request";
const LOGIN_SUCCESSFUL = "scm/auth/login_successful";
const LOGIN_FAILED = "scm/auth/login_failed";

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
  console.log(login_data);
  return function(dispatch) {
    dispatch(loginRequest());
    return fetch("/api/rest/v2/auth/access_token", {
      method: "POST",
      headers: {
        "Content-Type": "application/json; charset=utf-8"
      },
      body: JSON.stringify(login_data)
    }).then(
      response => {
        if (response.ok) {
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

export default function reducer(state = {}, action = {}) {
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

    default:
      return state;
  }
}
