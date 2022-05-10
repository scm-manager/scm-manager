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
import { createStore, Reducer } from "redux";
import { IndexResources, Links, Me } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { Provider } from "react-redux";
import ReduxLegacy from "./ReduxLegacy";

const ACTION_TYPE_INITIAL = "scm/initial";
const ACTION_TYPE_INDEX = "scm/index_success";
const ACTION_TYPE_ME = "scm/me_success";

type IndexActionSuccess = {
  type: typeof ACTION_TYPE_INDEX;
  payload: IndexResources;
};

type MeActionSuccess = {
  type: typeof ACTION_TYPE_ME;
  payload: Me;
};

type InitialAction = {
  type: typeof ACTION_TYPE_INITIAL;
};

export type ActionTypes = IndexActionSuccess | MeActionSuccess | InitialAction;

type State = {
  indexResources?: {
    version: string;
    links: Links;
  };
  auth?: {
    me?: Me;
  };
};

const initialState: State = {};

const reducer: Reducer<State, ActionTypes> = (
  state: State = initialState,
  action: ActionTypes = { type: ACTION_TYPE_INITIAL }
): State => {
  switch (action.type) {
    case ACTION_TYPE_INDEX: {
      return {
        ...state,
        indexResources: {
          version: action.payload.version,
          links: action.payload._links,
        },
      };
    }
    case ACTION_TYPE_ME: {
      return {
        ...state,
        auth: {
          me: action.payload,
        },
      };
    }
    default: {
      return state;
    }
  }
};

// add window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__() as last argument of createStore
// to enable redux devtools
const store = createStore(reducer);

export const fetchIndexResourcesSuccess = (index: IndexResources): IndexActionSuccess => {
  return {
    type: ACTION_TYPE_INDEX,
    payload: index,
  };
};

export const fetchMeSuccess = (me: Me): MeActionSuccess => {
  return {
    type: ACTION_TYPE_ME,
    payload: me,
  };
};

const LegacyReduxProvider: FC = ({ children }) => (
  <Provider store={store}>
    <ReduxLegacy>{children}</ReduxLegacy>
  </Provider>
);

export default LegacyReduxProvider;
