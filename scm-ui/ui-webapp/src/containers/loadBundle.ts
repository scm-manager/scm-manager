/* global SystemJS */
// eslint-disable-next-line import/no-webpack-loader-syntax
import "script-loader!../../../../node_modules/systemjs/dist/system.js";

import * as React from "react";
import * as ReactDOM from "react-dom";
import * as ReactRouterDom from "react-router-dom";
import * as Redux from "redux";
import * as ReactRedux from "react-redux";
import SytleComponentsDefault from "styled-components";
import * as SytleComponents from "styled-components";
import * as ReactI18Next from "react-i18next";
import ClassNamesDefault from "classnames";
import * as ClassNames from "classnames";
import QueryStringDefault from "query-string";
import * as QueryString from "query-string";
import * as UIExtensions from "@scm-manager/ui-extensions";
import * as UIComponents from "@scm-manager/ui-components";
import { urls } from "@scm-manager/ui-components";

// TODO add headers "Cache": "no-cache", "X-Requested-With": "XMLHttpRequest"

SystemJS.config({
  baseURL: urls.withContextPath("/assets"),
  meta: {
    "/*": {
      esModule: true,
      authorization: true
    }
  }
});

const expose = (name, cmp, defaultCmp) => {
  let mod = cmp;
  if (defaultCmp) {
    // SystemJS default export:
    // https://github.com/systemjs/systemjs/issues/1749
    mod = {
      ...cmp,
      __useDefault: defaultCmp
    };
  }
  SystemJS.set(name, SystemJS.newModule(mod));
};

expose("react", React);
expose("react-dom", ReactDOM);
expose("react-router-dom", ReactRouterDom);
expose("styled-components", SytleComponents, SytleComponentsDefault);
expose("redux", Redux);
expose("react-redux", ReactRedux);
expose("react-i18next", ReactI18Next);
expose("classnames", ClassNames, ClassNamesDefault);
expose("query-string", QueryString, QueryStringDefault);
expose("@scm-manager/ui-extensions", UIExtensions);
expose("@scm-manager/ui-components", UIComponents);

export default plugin => SystemJS.import(plugin);
