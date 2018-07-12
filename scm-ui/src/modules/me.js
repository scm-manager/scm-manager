//@flow
import { apiClient, NOT_AUTHENTICATED_ERROR } from "../apiclient";
import type { Me } from "../types/me";

const AUTHENTICATION_INFO_URL = "/me";

export const ME_AUTHENTICATED_REQUEST = "scm/auth/me_request";
export const ME_AUTHENTICATED_SUCCESS = "scm/auth/me_success";
export const ME_AUTHENTICATED_FAILURE = "scm/auth/me_failure";
export const ME_UNAUTHENTICATED = "scm/auth/me_unauthenticated";

export function meRequest() {
  return {
    type: ME_AUTHENTICATED_REQUEST
  };
}

export function meSuccess(user: Me) {
  return {
    type: ME_AUTHENTICATED_SUCCESS,
    payload: user
  };
}

export function meFailure(error: Error) {
  return {
    type: ME_AUTHENTICATED_FAILURE,
    payload: error
  };
}

export function meUnauthenticated() {
  return {
    type: ME_UNAUTHENTICATED
  };
}

export function fetchMe() {
  return function(dispatch: any => void) {
    dispatch(meRequest());
    return apiClient
      .get(AUTHENTICATION_INFO_URL)
      .then(response => {
        return response.json();
      })
      .then(data => {
        if (data) {
          dispatch(meSuccess(data));
        }
      })
      .catch((error: Error) => {
        if (error === NOT_AUTHENTICATED_ERROR) {
          dispatch(meUnauthenticated());
        } else {
          dispatch(meFailure(error));
        }
      });
  };
}

export default function reducer(
  state: any = { loading: true },
  action: any = {}
) {
  switch (action.type) {
    case ME_AUTHENTICATED_REQUEST:
      return {
        ...state,
        loading: true,
        me: null,
        error: null
      };
    case ME_AUTHENTICATED_SUCCESS:
      return {
        ...state,
        loading: false,
        me: action.payload,
        error: null
      };
    case ME_AUTHENTICATED_FAILURE:
      return {
        ...state,
        loading: false,
        me: null,
        error: action.payload
      };
    case ME_UNAUTHENTICATED:
      return {
        ...state,
        loading: false,
        me: null,
        error: null
      };

    default:
      return state;
  }
}
