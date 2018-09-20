// @flow
import thunk from "redux-thunk";
import logger from "redux-logger";
import { createStore, compose, applyMiddleware, combineReducers } from "redux";
import { routerReducer, routerMiddleware } from "react-router-redux";

import users from "./users/modules/users";
import repos from "./repos/modules/repos";
import repositoryTypes from "./repos/modules/repositoryTypes";
import changesets from "./repos/modules/changesets";
import groups from "./groups/modules/groups";
import auth from "./modules/auth";
import pending from "./modules/pending";
import failure from "./modules/failure";
import config from "./config/modules/config";

import type { BrowserHistory } from "history/createBrowserHistory";
import branches from "./repos/modules/branches";

function createReduxStore(history: BrowserHistory) {
  const composeEnhancers =
    window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

  const reducer = combineReducers({
    router: routerReducer,
    pending,
    failure,
    users,
    repos,
    repositoryTypes,
    changesets,
    branches,
    groups,
    auth,
    config
  });

  return createStore(
    reducer,
    composeEnhancers(applyMiddleware(routerMiddleware(history), thunk, logger))
  );
}

export default createReduxStore;
