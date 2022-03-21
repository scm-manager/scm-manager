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

import { define, defineLazy, defineStatic, load } from "@scm-manager/ui-modules";

import * as React from "react";
import * as ReactDOM from "react-dom";
import * as ReactRouterDom from "react-router-dom";
import * as ReactQuery from "react-query";
import * as StyledComponents from "styled-components";
import * as ReactHookForm from "react-hook-form";
import * as ReactI18Next from "react-i18next";
import * as ClassNames from "classnames";
import * as QueryString from "query-string";
import * as UIExtensions from "@scm-manager/ui-extensions";
import * as UIComponents from "@scm-manager/ui-components";
import * as UIApi from "@scm-manager/ui-api";

declare global {
  interface Window {
    define: typeof define;
  }
}

window.define = define;

defineStatic("react", React);
defineStatic("react-dom", ReactDOM);
defineStatic("react-router-dom", ReactRouterDom);
defineStatic("styled-components", StyledComponents);
defineStatic("react-i18next", ReactI18Next);
defineStatic("react-hook-form", ReactHookForm);
defineStatic("react-query", ReactQuery);
defineStatic("classnames", ClassNames);
defineStatic("query-string", QueryString);
defineStatic("@scm-manager/ui-extensions", UIExtensions);
defineStatic("@scm-manager/ui-components", UIComponents);
defineStatic("@scm-manager/ui-api", UIApi);

// redux is deprecated in favor of ui-api
defineLazy("redux", () => import("@scm-manager/ui-legacy").then(legacy => legacy.Redux));
defineLazy("react-redux", () => import("@scm-manager/ui-legacy").then(legacy => legacy.ReactRedux));

export default load;
