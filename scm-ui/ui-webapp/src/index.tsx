/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React from "react";
import Index from "./containers/Index";
import ReactDOM from "react-dom/client";

import { I18nextProvider } from "react-i18next";
import i18n from "./i18n";

import { ActiveModalCountContextProvider } from "@scm-manager/ui-components";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import ChangesetShortLink from "./repos/components/changesets/ChangesetShortLink";

import "./tokenExpired";
import { ApiProvider } from "@scm-manager/ui-api";
import { ShortcutDocsContextProvider } from "@scm-manager/ui-core"; // Makes sure that the global `define` function is registered and all provided modules are included in the final bundle at all times
import "./_modules/provided-modules";

async function startApp() {
  binder.bind<extensionPoints.ChangesetDescriptionTokens>("changeset.description.tokens", ChangesetShortLink);

  const container = document.getElementById("root");
  if (!container) {
    throw new Error("could not find root element");
  }

  const root = ReactDOM.createRoot(container);
  root.render(
    <ApiProvider>
      <I18nextProvider i18n={i18n}>
        <ShortcutDocsContextProvider>
          <ActiveModalCountContextProvider>
            <Index />
          </ActiveModalCountContextProvider>
        </ShortcutDocsContextProvider>
      </I18nextProvider>
    </ApiProvider>,
  );
}

startApp();
