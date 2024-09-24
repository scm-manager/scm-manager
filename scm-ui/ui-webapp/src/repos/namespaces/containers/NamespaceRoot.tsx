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

import React, { FC, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useRouteMatch } from "react-router-dom";
import {
  CustomQueryFlexWrappedColumns,
  ErrorPage,
  Loading,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  SubNavigation,
  urls,
} from "@scm-manager/ui-components";
import Permissions from "../../permissions/containers/Permissions";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import PermissionsNavLink from "./PermissionsNavLink";
import { useNamespace, useNamespaceAndNameContext } from "@scm-manager/ui-api";
import NamespaceInformation from "./NamespaceInformation";

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
    <Page title={namespace.namespace}>
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>
          <Switch>
            <Route path={`${url}/info`}>
              <NamespaceInformation namespace={namespace} />
            </Route>
            <Route path={`${url}/settings/permissions`}>
              <Permissions namespaceOrRepository={namespace} />
            </Route>
            <ExtensionPoint<extensionPoints.NamespaceRoute>
              name="namespace.route"
              props={extensionProps}
              renderAll={true}
            />
          </Switch>
        </PrimaryContentColumn>
        <SecondaryNavigationColumn>
          <SecondaryNavigation label={t("namespaceRoot.menu.navigationLabel")}>
            <NavLink
              to={`${url}/info`}
              icon="fas fa-info-circle"
              label={t("namespaceRoot.menu.informationNavLink")}
              title={t("namespaceRoot.menu.informationNavLink")}
            />
            <ExtensionPoint<extensionPoints.NamespaceTopLevelNavigation>
              name="namespace.navigation.topLevel"
              props={extensionProps}
              renderAll={true}
            />
            {binder.hasExtension("namespace.setting", extensionProps) || namespace._links.permissions ? (
              <SubNavigation
                to={`${url}/settings/`}
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
            ) : null}
          </SecondaryNavigation>
        </SecondaryNavigationColumn>
      </CustomQueryFlexWrappedColumns>
    </Page>
  );
};

export default NamespaceRoot;
