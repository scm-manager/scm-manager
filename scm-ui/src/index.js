// @flow
import React from "react";
import ReactDOM from "react-dom";
import App from "./containers/App";
import registerServiceWorker from "./registerServiceWorker";

import { I18nextProvider } from "react-i18next";
import i18n from "./i18n";

import { Provider } from "react-redux";
import createHistory from "history/createBrowserHistory";

import type { BrowserHistory } from "history/createBrowserHistory";

import createReduxStore from "./createReduxStore";
import { ConnectedRouter } from "react-router-redux";

const publicUrl: string = process.env.PUBLIC_URL || "";

// Create a history of your choosing (we're using a browser history in this case)
const history: BrowserHistory = createHistory({
  basename: publicUrl
});

// Add the reducer to your store on the `router` key
// Also apply our middleware for navigating
const store = createReduxStore(history);

const root = document.getElementById("root");
if (!root) {
  throw new Error("could not find root element");
}

ReactDOM.render(
  <Provider store={store}>
    <I18nextProvider i18n={i18n}>
      {/* ConnectedRouter will use the store from Provider automatically */}
      <ConnectedRouter history={history}>
        <App />
      </ConnectedRouter>
    </I18nextProvider>
  </Provider>,
  root
);

registerServiceWorker();
