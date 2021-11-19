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
import React from "react";
import ReactDOM from "react-dom";
import Index from "./containers/Index";

import { I18nextProvider } from "react-i18next";
import i18n from "./i18n";

import { BrowserRouter as Router } from "react-router-dom";

import { urls } from "@scm-manager/ui-components";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import ChangesetShortLink from "./repos/components/changesets/ChangesetShortLink";

import "./tokenExpired";
import LegacyReduxProvider from "./LegacyReduxProvider";
import ReduxAwareApiProvider from "./ReduxAwareApiProvider";

binder.bind<extensionPoints.ChangesetDescriptionTokensExtension>("changeset.description.tokens", ChangesetShortLink);

const root = document.getElementById("root");
if (!root) {
  throw new Error("could not find root element");
}

ReactDOM.render(
  <LegacyReduxProvider>
    <ReduxAwareApiProvider>
      <I18nextProvider i18n={i18n}>
        <Router basename={urls.contextPath}>
          <Index />
        </Router>
      </I18nextProvider>
    </ReduxAwareApiProvider>
  </LegacyReduxProvider>,
  root
);
