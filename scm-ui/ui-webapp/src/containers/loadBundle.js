import "script-loader!../../../../node_modules/systemjs/dist/system.js";

import * as React from "react";
import * as ReactDOM from "react-dom";
import * as ReactRouterDom from "react-router-dom";
import * as ReactRedux from "react-redux";
import {default as injectSheets } from "react-jss";
import * as ReactJSS from "react-jss";
import * as ReactI18Next from "react-i18next";
import * as ClassNames from "classnames";
import * as UIExtensions from "@scm-manager/ui-extensions";
import * as UIComponents from "@scm-manager/ui-components";

/**
 credentials: "same-origin",
 headers: {
    Cache: "no-cache",
    // identify the request as ajax request
    "X-Requested-With": "XMLHttpRequest"
  }
*/

SystemJS.config({
  baseURL: "/plugins",
  meta: {
    "/*": {
      esModule: true,
      authorization: true
    }
  }
});

const expose = (name, cmp) => {
  SystemJS.set(name, SystemJS.newModule(cmp));
};

expose("react", React);
expose("react-dom", ReactDOM);
expose("react-router-dom", ReactRouterDom);
expose("react-jss", {...ReactJSS, default: injectSheets});
expose("react-redux", ReactRedux);
expose("react-i18next", ReactI18Next);
expose("classnames", ClassNames);
expose("@scm-manager/ui-extensions", UIExtensions);
expose("@scm-manager/ui-components", UIComponents);

export default (plugin: string) => SystemJS.import(plugin);
