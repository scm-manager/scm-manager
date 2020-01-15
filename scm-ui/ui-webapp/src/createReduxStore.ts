import thunk from "redux-thunk";
import logger from "redux-logger";
import { applyMiddleware, combineReducers, compose, createStore } from "redux";
import users from "./users/modules/users";
import repos from "./repos/modules/repos";
import repositoryTypes from "./repos/modules/repositoryTypes";
import changesets from "./repos/modules/changesets";
import sources from "./repos/sources/modules/sources";
import groups from "./groups/modules/groups";
import auth from "./modules/auth";
import pending from "./modules/pending";
import failure from "./modules/failure";
import permissions from "./repos/permissions/modules/permissions";
import config from "./admin/modules/config";
import roles from "./admin/roles/modules/roles";
import namespaceStrategies from "./admin/modules/namespaceStrategies";
import indexResources from "./modules/indexResource";
import plugins from "./admin/plugins/modules/plugins";

import branches from "./repos/branches/modules/branches";

function createReduxStore() {
  const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

  const reducer = combineReducers({
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

  return createStore(reducer, composeEnhancers(applyMiddleware(thunk, logger)));
}

export default createReduxStore;
