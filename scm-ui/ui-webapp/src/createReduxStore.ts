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

import thunk from "redux-thunk";
import logger from "redux-logger";
import { AnyAction, applyMiddleware, combineReducers, compose, createStore } from "redux";
import users from "./users/modules/users";
import repos from "./repos/modules/repos";
import repositoryTypes from "./repos/modules/repositoryTypes";
import changesets from "./repos/modules/changesets";
import sources from "./repos/sources/modules/sources";
import groups from "./groups/modules/groups";
import auth, { isAuthenticated } from "./modules/auth";
import pending from "./modules/pending";
import failure from "./modules/failure";
import permissions from "./repos/permissions/modules/permissions";
import config from "./admin/modules/config";
import roles from "./admin/roles/modules/roles";
import namespaceStrategies from "./admin/modules/namespaceStrategies";
import indexResources from "./modules/indexResource";
import plugins from "./admin/plugins/modules/plugins";
import { apiClient } from "@scm-manager/ui-components";

import branches from "./repos/branches/modules/branches";
import { UnauthorizedError } from "@scm-manager/ui-components/src";

function createReduxStore() {
  const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

  const appReducer = combineReducers({
    pending,
    failure,
    indexResources,
    users,
    repos,
    repositoryTypes,
    changesets,
    branches,
    permissions,
    groups,
    auth,
    config,
    roles,
    sources,
    namespaceStrategies,
    plugins
  });

  const reducer = (state: any, action: AnyAction) => {
    console.log(action.type, state?.tokenExpired);
    if (state?.tokenExpired && action.type.indexOf("FAILURE") === -1) {
      console.log("reset state");
      return appReducer({}, action);
    }

    if (action.type === "API_CLIENT_UNAUTHORIZED" && isAuthenticated(state)) {
      return { ...state, tokenExpired: true };
    }

    return { ...appReducer(state, action), tokenExpired: state?.tokenExpired };
  };

  const store = createStore(reducer, composeEnhancers(applyMiddleware(thunk, logger)));
  apiClient.onError(error => {
    if (error instanceof UnauthorizedError) {
      store.dispatch({ type: "API_CLIENT_UNAUTHORIZED", error });
    }
  });

  return store;
}

export default createReduxStore;
