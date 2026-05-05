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
import { Outlet, RouteObject, useParams, useRoutes, Navigate } from "react-router-dom";
import { useUser } from "@scm-manager/ui-api";
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
import { Loading, SINGLE_USER_SETTINGS_SUB_ROUTES, SINGLE_USER_SUB_ROUTES } from "@scm-manager/ui-core";
import { ExtensionPoint, extensionPoints, useRouteExtensions } from "@scm-manager/ui-extensions";
import { Details } from "./../components/table";
import {
  EditUserNavLink,
  SetApiKeysNavLink,
  SetPasswordNavLink,
  SetPermissionsNavLink,
  SetPublicKeysNavLink,
} from "./../components/navLinks";
import SetUserPermissions from "../../permissions/components/SetUserPermissions";
import SetUserPassword from "../components/SetUserPassword";
import SetPublicKeys from "../components/publicKeys/SetPublicKeys";
import SetApiKeys from "../components/apiKeys/SetApiKeys";
import EditUser from "./EditUser";

const SingleUser: FC = () => {
  const [t] = useTranslation("users");
  const { name } = useParams<{ name: string }>();
  const { isLoading, data: user, error } = useUser(name!);

  const extensionProps = {
    user: user!,
  };

  const extensionRoutes = useRouteExtensions<extensionPoints.UserRoute>({
    name: "user.route",
    props: extensionProps,
  });

  const userRoutes: RouteObject[] = user
    ? [
        {
          index: true,
          element: <Navigate to={SINGLE_USER_SUB_ROUTES.INFO} replace />,
        },
        {
          path: SINGLE_USER_SUB_ROUTES.INFO,
          element: <Details user={user} />,
        },
        {
          path: SINGLE_USER_SUB_ROUTES.SETTINGS,
          element: <Outlet />,
          children: [
            {
              index: true,
              element: <Navigate to={SINGLE_USER_SETTINGS_SUB_ROUTES.GENERAL} replace />,
            },
            {
              path: SINGLE_USER_SETTINGS_SUB_ROUTES.GENERAL,
              element: <EditUser user={user} />,
            },
            {
              path: SINGLE_USER_SETTINGS_SUB_ROUTES.PASSWORD,
              element: <SetUserPassword user={user} />,
            },
            {
              path: SINGLE_USER_SETTINGS_SUB_ROUTES.PERMISSIONS,
              element: <SetUserPermissions user={user} />,
            },
            {
              path: SINGLE_USER_SETTINGS_SUB_ROUTES.PUBLIC_KEYS,
              element: <SetPublicKeys user={user} />,
            },
            {
              path: SINGLE_USER_SETTINGS_SUB_ROUTES.API_KEYS,
              element: <SetApiKeys user={user} />,
            },
            ...extensionRoutes,
          ],
        },
      ]
    : [];

  const element = useRoutes(userRoutes);

  if (error) {
    return <ErrorPage title={t("singleUser.errorTitle")} subtitle={t("singleUser.errorSubtitle")} error={error} />;
  }

  if (!user || isLoading) {
    return <Loading />;
  }

  const basePath = `/user/${name}`;

  return (
    <Page title={user.displayName}>
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>{element}</PrimaryContentColumn>
        <SecondaryNavigationColumn>
          <SecondaryNavigation label={t("singleUser.menu.navigationLabel")}>
            <NavLink
              to={`${basePath}/${SINGLE_USER_SUB_ROUTES.INFO}`}
              icon="fas fa-info-circle"
              label={t("singleUser.menu.informationNavLink")}
              title={t("singleUser.menu.informationNavLink")}
              testId="user-information-link"
            />
            <SubNavigation
              to={`${basePath}/${SINGLE_USER_SUB_ROUTES.SETTINGS}/${SINGLE_USER_SETTINGS_SUB_ROUTES.GENERAL}`}
              label={t("singleUser.menu.settingsNavLink")}
              title={t("singleUser.menu.settingsNavLink")}
              testId="user-settings-link"
            >
              <EditUserNavLink
                user={user}
                editUrl={`${basePath}/${SINGLE_USER_SUB_ROUTES.SETTINGS}/${SINGLE_USER_SETTINGS_SUB_ROUTES.GENERAL}`}
              />
              <SetPasswordNavLink
                user={user}
                passwordUrl={`${basePath}/${SINGLE_USER_SUB_ROUTES.SETTINGS}/${SINGLE_USER_SETTINGS_SUB_ROUTES.PASSWORD}`}
              />
              <SetPermissionsNavLink
                user={user}
                permissionsUrl={`${basePath}/${SINGLE_USER_SUB_ROUTES.SETTINGS}/${SINGLE_USER_SETTINGS_SUB_ROUTES.PERMISSIONS}`}
              />
              <SetPublicKeysNavLink
                user={user}
                publicKeyUrl={`${basePath}/${SINGLE_USER_SUB_ROUTES.SETTINGS}/${SINGLE_USER_SETTINGS_SUB_ROUTES.PUBLIC_KEYS}`}
              />
              <SetApiKeysNavLink
                user={user}
                apiKeyUrl={`${basePath}/${SINGLE_USER_SUB_ROUTES.SETTINGS}/${SINGLE_USER_SETTINGS_SUB_ROUTES.API_KEYS}`}
              />
              <ExtensionPoint<extensionPoints.UserSetting> name="user.setting" props={extensionProps} renderAll />
            </SubNavigation>
          </SecondaryNavigation>
        </SecondaryNavigationColumn>
      </CustomQueryFlexWrappedColumns>
    </Page>
  );
};

export default SingleUser;
