// @flow
import type { Me } from "../types/Me";
import * as types from "./types";

import { apiClient, UNAUTHORIZED_ERROR } from "../apiclient";
import { isPending } from "./pending";
import { getFailure } from "./failure";

// Action

export const LOGIN = "scm/auth/LOGIN";
export const LOGIN_PENDING = `${LOGIN}_${types.PENDING_SUFFIX}`;
export const LOGIN_SUCCESS = `${LOGIN}_${types.SUCCESS_SUFFIX}`;
export const LOGIN_FAILURE = `${LOGIN}_${types.FAILURE_SUFFIX}`;

export const FETCH_ME = "scm/auth/FETCH_ME";
export const FETCH_ME_PENDING = `${FETCH_ME}_${types.PENDING_SUFFIX}`;
export const FETCH_ME_SUCCESS = `${FETCH_ME}_${types.SUCCESS_SUFFIX}`;
export const FETCH_ME_FAILURE = `${FETCH_ME}_${types.FAILURE_SUFFIX}`;
export const FETCH_ME_UNAUTHORIZED = `${FETCH_ME}_UNAUTHORIZED`;

export const LOGOUT = "scm/auth/LOGOUT";
export const LOGOUT_PENDING = `${LOGOUT}_${types.PENDING_SUFFIX}`;
export const LOGOUT_SUCCESS = `${LOGOUT}_${types.SUCCESS_SUFFIX}`;
export const LOGOUT_FAILURE = `${LOGOUT}_${types.FAILURE_SUFFIX}`;

// Reducer

const initialState = {};

export default function reducer(
  state: Object = initialState,
  action: Object = { type: "UNKNOWN" }
) {
  switch (action.type) {
    case LOGIN_SUCCESS:
    case FETCH_ME_SUCCESS:
      return {
        ...state,
        me: action.payload,
        authenticated: true
      };
    case FETCH_ME_UNAUTHORIZED:
      return {
        me: {},
        authenticated: false
      };
    case LOGOUT_SUCCESS:
      return initialState;

    default:
      return state;
  }
}

// Action Creators

export const loginPending = () => {
  return {
    type: LOGIN_PENDING
  };
};

export const loginSuccess = (me: Me) => {
  return {
    type: LOGIN_SUCCESS,
    payload: me
  };
};

export const loginFailure = (error: Error) => {
  return {
    type: LOGIN_FAILURE,
    payload: error
  };
};

export const logoutPending = () => {
  return {
    type: LOGOUT_PENDING
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

export const fetchMePending = () => {
  return {
    type: FETCH_ME_PENDING
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
    type: FETCH_ME_UNAUTHORIZED,
    resetPending: true
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

const callFetchMe = (): Promise<Me> => {
  return apiClient
    .get(ME_URL)
    .then(response => {
      return response.json();
    })
    .then(json => {
      return { name: json.name, displayName: json.displayName };
    });
};

export const login = (username: string, password: string) => {
  const login_data = {
    cookie: true,
    grant_type: "password",
    username,
    password
  };
  return function(dispatch: any) {
    dispatch(loginPending());
    return apiClient
      .post(LOGIN_URL, login_data)
      .then(response => {
        return callFetchMe();
      })
      .then(me => {
        dispatch(loginSuccess(me));
      })
      .catch(err => {
        dispatch(loginFailure(err));
      });
  };
};

export const fetchMe = () => {
  return function(dispatch: any) {
    dispatch(fetchMePending());
    return callFetchMe()
      .then(me => {
        dispatch(fetchMeSuccess(me));
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
    dispatch(logoutPending());
    return apiClient
      .delete(LOGIN_URL)
      .then(() => {
        dispatch(logoutSuccess());
      })
      .catch(error => {
        dispatch(logoutFailure(error));
      });
  };
};

// selectors

const stateAuth = (state: Object): Object => {
  return state.auth || {};
};

export const isAuthenticated = (state: Object) => {
  if (stateAuth(state).authenticated) {
    return true;
  }
  return false;
};

export const getMe = (state: Object): Me => {
  return stateAuth(state).me;
};

export const isFetchMePending = (state: Object) => {
  return isPending(state, FETCH_ME);
};

export const getFetchMeFailure = (state: Object) => {
  return getFailure(state, FETCH_ME);
};

export const isLoginPending = (state: Object) => {
  return isPending(state, LOGIN);
};

export const getLoginFailure = (state: Object) => {
  return getFailure(state, LOGIN);
};

export const isLogoutPending = (state: Object) => {
  return isPending(state, LOGOUT);
};

export const getLogoutFailure = (state: Object) => {
  return getFailure(state, LOGOUT);
};
