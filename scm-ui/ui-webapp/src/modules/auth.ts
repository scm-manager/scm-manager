/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import { Link, Me } from "@scm-manager/ui-types";
import * as types from "./types";

import { apiClient, UnauthorizedError } from "@scm-manager/ui-components";
import { isPending } from "./pending";
import { getFailure } from "./failure";
import {
  callFetchIndexResources,
  fetchIndexResources,
  fetchIndexResourcesPending,
  fetchIndexResourcesSuccess,
  getLoginLink
} from "./indexResource";
import { AnyAction } from "redux";

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
export const LOGOUT_REDIRECT = `${LOGOUT}_REDIRECT`;

// Reducer

const initialState = {};

export default function reducer(
  state: object = initialState,
  action: AnyAction = {
    type: "UNKNOWN"
  }
) {
  switch (action.type) {
    case LOGIN_SUCCESS:
    case FETCH_ME_SUCCESS:
      return {
        ...state,
        me: action.payload
      };
    case FETCH_ME_UNAUTHORIZED:
      return {
        me: {}
      };
    case LOGOUT_SUCCESS:
      return initialState;

    case LOGOUT_REDIRECT: {
      // we keep the current state until we are redirected to the new page
      return {
        ...state,
        redirecting: true
      };
    }
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

export const redirectAfterLogout = () => {
  return {
    type: LOGOUT_REDIRECT
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

// side effects

const callFetchMe = (link: string): Promise<Me> => {
  return apiClient.get(link).then(response => {
    return response.json();
  });
};

export const login = (loginLink: string, username: string, password: string) => {
  const loginData = {
    cookie: true,
    grant_type: "password",
    username,
    password
  };
  return function(dispatch: any) {
    dispatch(loginPending());
    return apiClient
      .post(loginLink, loginData)
      .then(() => {
        dispatch(fetchIndexResourcesPending());
        return callFetchIndexResources();
      })
      .then(response => {
        dispatch(fetchIndexResourcesSuccess(response));
        const meLink = (response._links.me as Link).href;
        return callFetchMe(meLink);
      })
      .then(me => {
        dispatch(loginSuccess(me));
      })
      .catch(err => {
        dispatch(loginFailure(err));
      });
  };
};

export const fetchMe = (link: string) => {
  return function(dispatch: any) {
    dispatch(fetchMePending());
    return callFetchMe(link)
      .then(me => {
        dispatch(fetchMeSuccess(me));
      })
      .catch((error: Error) => {
        if (error instanceof UnauthorizedError) {
          dispatch(fetchMeUnauthenticated());
        } else {
          dispatch(fetchMeFailure(error));
        }
      });
  };
};

export const logout = (link: string, callback: () => void) => {
  return function(dispatch: any) {
    dispatch(logoutPending());
    return apiClient
      .delete(link)
      .then(response => {
        return response.status === 200
          ? response.json()
          : new Promise(function(resolve) {
              resolve();
            });
      })
      .then(json => {
        let fetchIndex = true;
        if (json && json.logoutRedirect) {
          dispatch(redirectAfterLogout());
          window.location.assign(json.logoutRedirect);
          fetchIndex = false;
        } else {
          dispatch(logoutSuccess());
        }
        return fetchIndex;
      })
      .then((fetchIndex: boolean) => {
        if (fetchIndex) {
          dispatch(fetchIndexResources());
        }
      })
      .then(callback)
      .catch(error => {
        dispatch(logoutFailure(error));
      });
  };
};

// selectors

const stateAuth = (state: object): object => {
  // @ts-ignore Right types for redux not available
  return state.auth || {};
};

export const isAuthenticated = (state: object) => {
  // @ts-ignore Right types for redux not available
  return !!((state.auth.me && !getLoginLink(state)) || isAnonymous(state.auth.me));
};

export const getMe = (state: object): Me => {
  // @ts-ignore Right types for redux not available
  return stateAuth(state).me;
};

export const isFetchMePending = (state: object) => {
  return isPending(state, FETCH_ME);
};

export const getFetchMeFailure = (state: object) => {
  return getFailure(state, FETCH_ME);
};

export const isLoginPending = (state: object) => {
  return isPending(state, LOGIN);
};

export const getLoginFailure = (state: object) => {
  return getFailure(state, LOGIN);
};

export const isLogoutPending = (state: object) => {
  return isPending(state, LOGOUT);
};

export const getLogoutFailure = (state: object) => {
  return getFailure(state, LOGOUT);
};

export const isRedirecting = (state: object) => {
  // @ts-ignore Right types for redux not available
  return !!stateAuth(state).redirecting;
};

// Helper methods

export const isAnonymous = (me: Me) => {
  return me?.name === "_anonymous";
};
