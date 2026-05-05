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
import PluginLoader from "./PluginLoader";
import ScrollToTop from "./ScrollToTop";
import { NamespaceAndNameContextProvider, useIndex } from "@scm-manager/ui-api";
import { Link } from "@scm-manager/ui-types";
import i18next from "i18next";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import InitializationAdminAccountStep from "./InitializationAdminAccountStep";
import InitializationPluginWizardStep from "./InitializationPluginWizardStep";
import { Footer, Header } from "@scm-manager/ui-components";
import NavigationBar from "./NavigationBar";
import InitializationScm4xCompatibilityStep from "./InitializationScm4xCompatibilityStep";
import styled from "styled-components";

const MinViewportDiv = styled.div`
  min-height: 100vh;
`;

const Index: FC = () => {
  const data = useIndex();
  const [pluginsLoaded, setPluginsLoaded] = useState(false);

  i18next.on("languageChanged", (lng) => {
    document.documentElement.setAttribute("lang", lng);
  });

  if (data.initialization) {
    const Extension = binder.getExtension(`initialization.step.${data.initialization}`);
    return (
      <MinViewportDiv className="is-flex is-flex-direction-column">
        <Header authenticated={false}>
          <NavigationBar links={data._links} />
        </Header>
        <div className="is-flex-grow-1">
          <Extension data={data?._embedded ? data._embedded[data.initialization] : undefined} />
        </div>
        <Footer me={undefined} version={data.version} links={data._links} />
      </MinViewportDiv>
    );
  }

  const link = (data._links.uiPlugins as Link).href;
  return (
    <ScrollToTop>
      <NamespaceAndNameContextProvider>
        <PluginLoader link={link} loaded={pluginsLoaded} callback={() => setPluginsLoaded(true)}>
          <App />
        </PluginLoader>
      </NamespaceAndNameContextProvider>
    </ScrollToTop>
  );
};

export default Index;

binder.bind<extensionPoints.InitializationStep<"adminAccount">>(
  "initialization.step.adminAccount",
  InitializationAdminAccountStep,
);

binder.bind("initialization.step.pluginWizard", InitializationPluginWizardStep);
binder.bind("initialization.step.scm4xCompatibility", InitializationScm4xCompatibilityStep);
