/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
          links: action.payload._links
        }
      };
    }
    case ACTION_TYPE_ME: {
      return {
        ...state,
        auth: {
          me: action.payload
        }
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
    payload: index
  };
};

export const fetchMeSuccess = (me: Me): MeActionSuccess => {
  return {
    type: ACTION_TYPE_ME,
    payload: me
  };
};

const LegacyReduxProvider: FC = ({ children }) => (
  <Provider store={store}>
    <ReduxLegacy>{children}</ReduxLegacy>
  </Provider>
);

export default LegacyReduxProvider;
