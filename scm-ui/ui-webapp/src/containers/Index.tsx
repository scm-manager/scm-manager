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

import React, { FC, useState } from "react";
import App from "./App";
import { ErrorBoundary, Header, Loading } from "@scm-manager/ui-components";
import PluginLoader from "./PluginLoader";
import ScrollToTop from "./ScrollToTop";
import IndexErrorPage from "./IndexErrorPage";
import { NamespaceAndNameContextProvider, useIndex } from "@scm-manager/ui-api";
import { Link } from "@scm-manager/ui-types";
import i18next from "i18next";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import InitializationAdminAccountStep from "./InitializationAdminAccountStep";
import InitializationPluginWizardStep from "./InitializationPluginWizardStep";

const Index: FC = () => {
  const { isLoading, error, data } = useIndex();
  const [pluginsLoaded, setPluginsLoaded] = useState(false);

  // TODO check componentDidUpdate method for anonymous user stuff

  i18next.on("languageChanged", (lng) => {
    document.documentElement.setAttribute("lang", lng);
  });

  if (error) {
    return (
      <>
        <Header />
        <IndexErrorPage error={error} />
      </>
    );
  }
  if (isLoading || !data) {
    return <Loading />;
  }

  const link = (data._links.uiPlugins as Link).href;
  return (
    <ErrorBoundary fallback={IndexErrorPage}>
      <ScrollToTop>
        <NamespaceAndNameContextProvider>
          <PluginLoader link={link} loaded={pluginsLoaded} callback={() => setPluginsLoaded(true)}>
            <App />
          </PluginLoader>
        </NamespaceAndNameContextProvider>
      </ScrollToTop>
    </ErrorBoundary>
  );
};

export default Index;

binder.bind<extensionPoints.InitializationStep<"adminAccount">>(
  "initialization.step.adminAccount",
  InitializationAdminAccountStep
);

binder.bind<extensionPoints.InitializationStep<"pluginWizard">>(
  "initialization.step.pluginWizard",
  InitializationPluginWizardStep
);
