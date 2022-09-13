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
import React, { FC, useState } from "react";
import App from "./App";
import { ActiveModalCountContext, ErrorBoundary, Header, Loading } from "@scm-manager/ui-components";
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
  const [modalCount, setModalCount] = useState(0);

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
    <ActiveModalCountContext.Provider value={{ value: modalCount, setValue: setModalCount }}>
      <ErrorBoundary fallback={IndexErrorPage}>
        <ScrollToTop>
          <NamespaceAndNameContextProvider>
            <PluginLoader link={link} loaded={pluginsLoaded} callback={() => setPluginsLoaded(true)}>
              <App />
            </PluginLoader>
          </NamespaceAndNameContextProvider>
        </ScrollToTop>
      </ErrorBoundary>
    </ActiveModalCountContext.Provider>
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
