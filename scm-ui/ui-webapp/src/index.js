// @flow
import React from "react";
import ReactDOM from "react-dom";
import Index from "./containers/Index";
import registerServiceWorker from "./registerServiceWorker";

import { I18nextProvider } from "react-i18next";
import i18n from "./i18n";

import { Provider } from "react-redux";

import createReduxStore from "./createReduxStore";
import { BrowserRouter as Router } from "react-router-dom";

import { urls } from "@scm-manager/ui-components";

const store = createReduxStore();

const root = document.getElementById("root");
if (!root) {
  throw new Error("could not find root element");
}

ReactDOM.render(
  <Provider store={store}>
    <I18nextProvider i18n={i18n}>
      <Router basename={urls.contextPath}>
        <Index />
      </Router>
    </I18nextProvider>
  </Provider>,
  root
);

registerServiceWorker();
