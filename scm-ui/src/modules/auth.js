// @flow
import type { Me } from "../types/me";

import { apiClient, UNAUTHORIZED_ERROR } from "../apiclient";

// Action

export const LOGIN_REQUEST = "scm/auth/LOGIN_REQUEST";
export const LOGIN_SUCCESS = "scm/auth/LOGIN_SUCCESS";
export const LOGIN_FAILURE = "scm/auth/LOGIN_FAILURE";

export const FETCH_ME_REQUEST = "scm/auth/FETCH_ME_REQUEST";
export const FETCH_ME_SUCCESS = "scm/auth/FETCH_ME_SUCCESS";
export const FETCH_ME_FAILURE = "scm/auth/FETCH_ME_FAILURE";
export const FETCH_ME_UNAUTHORIZED = "scm/auth/FETCH_ME_UNAUTHORIZED";

export const LOGOUT_REQUEST = "scm/auth/LOGOUT_REQUEST";
export const LOGOUT_SUCCESS = "scm/auth/LOGOUT_SUCCESS";
export const LOGOUT_FAILURE = "scm/auth/LOGOUT_FAILURE";

// Reducer

const initialState = {
  me: { loading: true }
};

export default function reducer(state: any = initialState, action: any = {}) {
  switch (action.type) {
    case LOGIN_REQUEST:
      return {
        ...state,
        login: {
          loading: true
        }
      };
    case LOGIN_SUCCESS:
      return {
        ...state,
        login: {
          authenticated: true
        }
      };
    case LOGIN_FAILURE:
      return {
        ...state,
        login: {
          error: action.payload
        }
      };

    case FETCH_ME_REQUEST:
      return {
        ...state,
        me: {
          loading: true
        }
      };
    case FETCH_ME_SUCCESS:
      return {
        ...state,
        me: {
          entry: action.payload
        },
        login: {
          authenticated: true
        }
      };
    case FETCH_ME_UNAUTHORIZED:
      return {
        ...state,
        me: {},
        login: {
          authenticated: false
        }
      };
    case FETCH_ME_FAILURE:
      return {
        ...state,
        me: {
          error: action.payload
        }
      };

    case LOGOUT_REQUEST:
      return {
        ...state,
        logout: {
          loading: true
        }
      };
    case LOGOUT_SUCCESS:
      return initialState;
    case LOGOUT_FAILURE:
      return {
        ...state,
        logout: {
          error: action.payload
        }
      };
    default:
      return state;
  }
}

// Action Creators

export const loginRequest = () => {
  return {
    type: LOGIN_REQUEST
  };
};

export const loginSuccess = () => {
  return {
    type: LOGIN_SUCCESS
  };
};

export const loginFailure = (error: Error) => {
  return {
    type: LOGIN_FAILURE,
    payload: error
  };
};

export const logoutRequest = () => {
  return {
    type: LOGOUT_REQUEST
  };
};

export const logoutSuccess = () => {
  return {
    type: LOGOUT_SUCCESS
  };
};

export const logoutFailure = (error: Error) => {
  return {
    type: LOGOUT_FAILURE,
    payload: error
  };
};

export const fetchMeRequest = () => {
  return {
    type: FETCH_ME_REQUEST
  };
};

export const fetchMeSuccess = (me: Me) => {
  return {
    type: FETCH_ME_SUCCESS,
    payload: me
  };
};

export const fetchMeUnauthenticated = () => {
  return {
    type: FETCH_ME_UNAUTHORIZED
  };
};

export const fetchMeFailure = (error: Error) => {
  return {
    type: FETCH_ME_FAILURE,
    payload: error
  };
};

// urls

const ME_URL = "/me";
const LOGIN_URL = "/auth/access_token";

// side effects

export const login = (username: string, password: string) => {
  const login_data = {
    cookie: true,
    grant_type: "password",
    username,
    password
  };
  return function(dispatch: any) {
    dispatch(loginRequest());
    return apiClient
      .post(LOGIN_URL, login_data)
      .then(response => {
        dispatch(fetchMe());
        dispatch(loginSuccess());
      })
      .catch(err => {
        dispatch(loginFailure(err));
      });
  };
};

export const fetchMe = () => {
  return function(dispatch: any) {
    dispatch(fetchMeRequest());
    return apiClient
      .get(ME_URL)
      .then(response => {
        return response.json();
      })
      .then(me => {
        dispatch(
          fetchMeSuccess({ userName: me.name, displayName: me.displayName })
        );
      })
      .catch((error: Error) => {
        if (error === UNAUTHORIZED_ERROR) {
          dispatch(fetchMeUnauthenticated());
        } else {
          dispatch(fetchMeFailure(error));
        }
      });
  };
};

export const logout = () => {
  return function(dispatch: any) {
    dispatch(logoutRequest());
    return apiClient
      .delete(LOGIN_URL)
      .then(() => {
        dispatch(logoutSuccess());
        dispatch(fetchMe());
      })
      .catch(error => {
        dispatch(logoutFailure(error));
      });
  };
};

// selectors

export const isAuthenticated = (state: any): boolean => {
  return state.auth && state.auth.login && state.auth.login.authenticated;
};
