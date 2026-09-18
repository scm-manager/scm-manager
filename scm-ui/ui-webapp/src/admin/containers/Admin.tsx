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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Outlet } from "react-router-dom";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import {
  CustomQueryFlexWrappedColumns,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  SubNavigation,
} from "@scm-manager/ui-components";
import { useIndexLinks } from "@scm-manager/ui-api";
import {
  ADMIN_BASE_ROUTE,
  ADMIN_PLUGINS_SUB_ROUTES,
  ADMIN_SETTINGS_SUB_ROUTES,
  ADMIN_SUB_ROUTES,
} from "@scm-manager/ui-core";

const Admin: FC = () => {
  const links = useIndexLinks();
  const [t] = useTranslation("admin");
  const availablePluginsLink = links.availablePlugins;
  const installedPluginsLink = links.installedPlugins;

  const extensionProps = {
    links,
  };

  return (
    <Page>
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>
          <Outlet />
        </PrimaryContentColumn>
        <SecondaryNavigationColumn>
          <SecondaryNavigation label={t("admin.menu.navigationLabel")}>
            <NavLink
              to={`/${ADMIN_BASE_ROUTE}/${ADMIN_SUB_ROUTES.INFO}`}
              icon="fas fa-info-circle"
              label={t("admin.menu.informationNavLink")}
              title={t("admin.menu.informationNavLink")}
              testId="admin-information-link"
            />
            {(availablePluginsLink || installedPluginsLink) && (
              <SubNavigation
                to={`/${ADMIN_BASE_ROUTE}/${ADMIN_SUB_ROUTES.PLUGINS}/`}
                icon="fas fa-puzzle-piece"
                label={t("plugins.menu.pluginsNavLink")}
                title={t("plugins.menu.pluginsNavLink")}
                testId="admin-plugins-link"
              >
                {installedPluginsLink && (
                  <NavLink
                    to={`/${ADMIN_BASE_ROUTE}/${ADMIN_SUB_ROUTES.PLUGINS}/${ADMIN_PLUGINS_SUB_ROUTES.INSTALLED}/`}
                    label={t("plugins.menu.installedNavLink")}
                    testId="admin-installed-plugins-link"
                  />
                )}
                {availablePluginsLink && (
                  <NavLink
                    to={`/${ADMIN_BASE_ROUTE}/${ADMIN_SUB_ROUTES.PLUGINS}/${ADMIN_PLUGINS_SUB_ROUTES.AVAILABLE}/`}
                    label={t("plugins.menu.availableNavLink")}
                    testId="admin-available-plugins-link"
                  />
                )}
              </SubNavigation>
            )}
            <NavLink
              to={`/${ADMIN_BASE_ROUTE}/${ADMIN_SUB_ROUTES.ROLES}`}
              icon="fas fa-user-shield"
              label={t("repositoryRole.navLink")}
              title={t("repositoryRole.navLink")}
              testId="admin-repository-role-link"
              activeWhenMatch={new RegExp(`${ADMIN_SUB_ROUTES.ROLE}/`)}
            />
            <ExtensionPoint<extensionPoints.AdminNavigation>
              name="admin.navigation"
              props={extensionProps}
              renderAll={true}
            />
            <SubNavigation
              to={`/${ADMIN_BASE_ROUTE}/${ADMIN_SUB_ROUTES.SETTINGS}/${ADMIN_SETTINGS_SUB_ROUTES.GENERAL}/`}
              label={t("admin.menu.settingsNavLink")}
              title={t("admin.menu.settingsNavLink")}
              testId="admin-settings-link"
              activeWhenMatch={/settings\/.+/}
            >
              <NavLink
                to={`/${ADMIN_BASE_ROUTE}/${ADMIN_SUB_ROUTES.SETTINGS}/${ADMIN_SETTINGS_SUB_ROUTES.GENERAL}/`}
                label={t("admin.menu.generalNavLink")}
                testId="admin-settings-general-link"
              />
              <ExtensionPoint<extensionPoints.AdminSetting>
                name="admin.setting"
                props={extensionProps}
                renderAll={true}
              />
            </SubNavigation>
          </SecondaryNavigation>
        </SecondaryNavigationColumn>
      </CustomQueryFlexWrappedColumns>
    </Page>
  );
};

export default Admin;
