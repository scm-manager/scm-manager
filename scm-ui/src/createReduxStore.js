// @flow
import thunk from "redux-thunk";
import logger from "redux-logger";
import { createStore, compose, applyMiddleware, combineReducers } from "redux";
import { routerReducer, routerMiddleware } from "react-router-redux";

import repositories from "./repositories/modules/repositories";
import users from "./users/modules/users";
import auth from "./modules/auth";

import type { BrowserHistory } from "history/createBrowserHistory";

function createReduxStore(history: BrowserHistory) {
  const composeEnhancers =
    window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

  const reducer = combineReducers({
    router: routerReducer,
    repositories,
    users,
    auth
  });

  return createStore(
    reducer,
    composeEnhancers(applyMiddleware(routerMiddleware(history), thunk, logger))
  );
}

export default createReduxStore;
