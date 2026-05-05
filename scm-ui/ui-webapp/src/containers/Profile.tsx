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
import { Outlet } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useMe } from "@scm-manager/ui-api";
import {
  CustomQueryFlexWrappedColumns,
  ErrorPage,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  SubNavigation,
} from "@scm-manager/ui-components";
import { ME_BASE_ROUTE, ME_SETTINGS_SUB_ROUTES, ME_SUB_ROUTES } from "@scm-manager/ui-core";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import SetPublicKeysNavLink from "../users/components/navLinks/SetPublicKeysNavLink";
import SetApiKeysNavLink from "../users/components/navLinks/SetApiKeysNavLink";

const Profile: FC = () => {
  const [t] = useTranslation("commons");
  const { data: me, isLoading } = useMe();

  if (isLoading) {
    return <></>;
  }

  if (!me) {
    return (
      <ErrorPage
        title={t("profile.error-title")}
        subtitle={t("profile.error-subtitle")}
        error={{
          name: t("profile.error"),
          message: t("profile.error-message"),
        }}
      />
    );
  }

  const mayChangePassword = !!me._links.password;

  return (
    <Page title={me.displayName}>
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>
          <Outlet />
        </PrimaryContentColumn>
        <SecondaryNavigationColumn>
          <SecondaryNavigation label={t("profile.navigationLabel")}>
            <NavLink
              to={`/${ME_BASE_ROUTE}/${ME_SUB_ROUTES.INFO}`}
              icon="fas fa-info-circle"
              label={t("profile.informationNavLink")}
              title={t("profile.informationNavLink")}
            />
            <SubNavigation
              to={`/${ME_BASE_ROUTE}/${ME_SUB_ROUTES.SETTINGS}/${ME_SETTINGS_SUB_ROUTES.THEME}`}
              label={t("profile.settingsNavLink")}
              title={t("profile.settingsNavLink")}
            >
              <NavLink
                to={`/${ME_BASE_ROUTE}/${ME_SUB_ROUTES.SETTINGS}/${ME_SETTINGS_SUB_ROUTES.THEME}`}
                label={t("profile.theme.navLink")}
              />
              <NavLink
                to={`/${ME_BASE_ROUTE}/${ME_SUB_ROUTES.SETTINGS}/${ME_SETTINGS_SUB_ROUTES.ACCESSIBILITY}`}
                label={t("profile.accessibility.navLink")}
              />
              {mayChangePassword && (
                <NavLink
                  to={`/${ME_BASE_ROUTE}/${ME_SUB_ROUTES.SETTINGS}/${ME_SETTINGS_SUB_ROUTES.PASSWORD}`}
                  label={t("profile.changePasswordNavLink")}
                />
              )}
              <SetPublicKeysNavLink
                user={me}
                publicKeyUrl={`/${ME_BASE_ROUTE}/${ME_SUB_ROUTES.SETTINGS}/${ME_SETTINGS_SUB_ROUTES.PUBLIC_KEYS}`}
              />
              <SetApiKeysNavLink
                user={me}
                apiKeyUrl={`/${ME_BASE_ROUTE}/${ME_SUB_ROUTES.SETTINGS}/${ME_SETTINGS_SUB_ROUTES.API_KEYS}`}
              />
              <ExtensionPoint name="profile.setting" props={{ me }} renderAll />
            </SubNavigation>
          </SecondaryNavigation>
        </SecondaryNavigationColumn>
      </CustomQueryFlexWrappedColumns>
    </Page>
  );
};

export default Profile;
