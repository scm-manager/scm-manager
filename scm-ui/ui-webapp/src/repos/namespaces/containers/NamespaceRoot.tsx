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

import React, { FC, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch, useRouteMatch } from "react-router-dom";
import {
  CustomQueryFlexWrappedColumns,
  ErrorPage,
  Loading,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  StateMenuContextProvider,
  SubNavigation,
  urls,
} from "@scm-manager/ui-components";
import Permissions from "../../permissions/containers/Permissions";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import PermissionsNavLink from "./PermissionsNavLink";
import { useNamespace, useNamespaceAndNameContext } from "@scm-manager/ui-api";

type Params = {
  namespaceName: string;
};

const NamespaceRoot: FC = () => {
  const match = useRouteMatch<Params>();
  const { isLoading, error, data: namespace } = useNamespace(match.params.namespaceName);
  const [t] = useTranslation("namespaces");
  const url = urls.matchedUrlFromMatch(match);
  const context = useNamespaceAndNameContext();

  useEffect(() => {
    if (namespace) {
      context.setNamespace(namespace.namespace);
    }
    return () => {
      context.setNamespace("");
    };
  }, [namespace, context]);

  if (error) {
    return (
      <ErrorPage title={t("namespaceRoot.errorTitle")} subtitle={t("namespaceRoot.errorSubtitle")} error={error} />
    );
  }

  if (!namespace || isLoading) {
    return <Loading />;
  }

  const extensionProps = {
    namespace,
    url,
  };

  return (
    <StateMenuContextProvider>
      <Page title={namespace.namespace}>
        <CustomQueryFlexWrappedColumns>
          <PrimaryContentColumn>
            <Switch>
              <Redirect exact from={`${url}/settings`} to={`${url}/settings/permissions`} />
              <Route path={`${url}/settings/permissions`}>
                <Permissions namespaceOrRepository={namespace} />
              </Route>
            </Switch>
          </PrimaryContentColumn>
          <SecondaryNavigationColumn>
            <SecondaryNavigation label={t("namespaceRoot.menu.navigationLabel")}>
              <ExtensionPoint<extensionPoints.NamespaceTopLevelNavigation>
                name="namespace.navigation.topLevel"
                props={extensionProps}
                renderAll={true}
              />
              <ExtensionPoint<extensionPoints.NamespaceRoute>
                name="namespace.route"
                props={extensionProps}
                renderAll={true}
              />
              <SubNavigation
                to={`${url}/settings`}
                label={t("namespaceRoot.menu.settingsNavLink")}
                title={t("namespaceRoot.menu.settingsNavLink")}
              >
                <PermissionsNavLink permissionUrl={`${url}/settings/permissions`} namespace={namespace} />
                <ExtensionPoint<extensionPoints.NamespaceSetting>
                  name="namespace.setting"
                  props={extensionProps}
                  renderAll={true}
                />
              </SubNavigation>
            </SecondaryNavigation>
          </SecondaryNavigationColumn>
        </CustomQueryFlexWrappedColumns>
      </Page>
    </StateMenuContextProvider>
  );
};

export default NamespaceRoot;
