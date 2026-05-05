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
import { Outlet } from "react-router-dom";
import {
  CustomQueryFlexWrappedColumns,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  SubNavigation,
} from "@scm-manager/ui-components";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import PermissionsNavLink from "./PermissionsNavLink";
import { useIndexLinks, useNamespaceAndNameContext } from "@scm-manager/ui-api";
import {
  NAMESPACE_SETTINGS_SUB_ROUTES,
  NAMESPACE_SUB_ROUTES,
  SINGLE_NAMESPACE_BASE_ROUTE,
  useCurrentNamespaceBaseUrl,
  useNamespaceFromParams,
} from "@scm-manager/ui-core";

const NamespaceRoot: FC = () => {
  const namespace = useNamespaceFromParams();
  const [t] = useTranslation("namespaces");
  const context = useNamespaceAndNameContext();
  const indexLinks = useIndexLinks();
  const absoluteNamespaceUrl = useCurrentNamespaceBaseUrl();

  const basePath = `/${SINGLE_NAMESPACE_BASE_ROUTE}/${namespace.namespace}`;

  const extensionProps = {
    namespace,
    indexLinks,
    absoluteNamespaceUrl,
  };

  useEffect(() => {
    if (namespace) {
      context.setNamespace(namespace.namespace);
    }
    return () => {
      context.setNamespace("");
    };
  }, [namespace, context]);

  return (
    <Page title={namespace.namespace}>
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>
          <Outlet />
        </PrimaryContentColumn>
        <SecondaryNavigationColumn>
          <SecondaryNavigation label={t("namespaceRoot.menu.navigationLabel")}>
            <NavLink
              to={`${basePath}/${NAMESPACE_SUB_ROUTES.INFO}`}
              icon="fas fa-info-circle"
              label={t("namespaceRoot.menu.informationNavLink")}
              title={t("namespaceRoot.menu.informationNavLink")}
            />
            <ExtensionPoint<extensionPoints.NamespaceTopLevelNavigation>
              name="namespace.navigation.topLevel"
              props={extensionProps}
              renderAll
            />
            {binder.hasExtension("namespace.setting", extensionProps) || namespace._links.permissions ? (
              <SubNavigation
                to={`${basePath}/${NAMESPACE_SUB_ROUTES.SETTINGS}/${NAMESPACE_SETTINGS_SUB_ROUTES.PERMISSIONS}`}
                label={t("namespaceRoot.menu.settingsNavLink")}
                title={t("namespaceRoot.menu.settingsNavLink")}
              >
                <PermissionsNavLink
                  permissionUrl={`${basePath}/${NAMESPACE_SUB_ROUTES.SETTINGS}/${NAMESPACE_SETTINGS_SUB_ROUTES.PERMISSIONS}`}
                  namespace={namespace}
                />
                <ExtensionPoint<extensionPoints.NamespaceSetting>
                  name="namespace.setting"
                  props={extensionProps}
                  renderAll
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
