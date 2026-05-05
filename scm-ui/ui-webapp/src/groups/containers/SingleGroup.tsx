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
import { useGroup } from "@scm-manager/ui-api";
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
import { Loading, SINGLE_GROUP_SETTINGS_SUB_ROUTES, SINGLE_GROUP_SUB_ROUTES } from "@scm-manager/ui-core";
import { ExtensionPoint, extensionPoints, useRouteExtensions } from "@scm-manager/ui-extensions";
import { Details } from "./../components/table";
import { EditGroupNavLink, SetPermissionsNavLink } from "./../components/navLinks";
import SetGroupPermissions from "../../permissions/components/SetGroupPermissions";
import EditGroup from "./EditGroup";

const SingleGroup: FC = () => {
  const [t] = useTranslation("groups");
  const { name } = useParams<{ name: string }>();
  const { data: group, isLoading, error } = useGroup(name);

  const extensionProps = {
    group: group!,
  };

  const extensionRoutes = useRouteExtensions<extensionPoints.GroupRoute>({
    name: "group.route",
    props: extensionProps,
  });

  const groupRoutes: RouteObject[] = group
    ? [
        {
          index: true,
          element: <Navigate to={SINGLE_GROUP_SUB_ROUTES.INFO} replace />,
        },
        {
          path: SINGLE_GROUP_SUB_ROUTES.INFO,
          element: <Details group={group} />,
        },
        {
          path: SINGLE_GROUP_SUB_ROUTES.SETTINGS,
          element: <Outlet />,
          children: [
            {
              index: true,
              element: <Navigate to={SINGLE_GROUP_SETTINGS_SUB_ROUTES.GENERAL} replace />,
            },
            {
              path: SINGLE_GROUP_SETTINGS_SUB_ROUTES.GENERAL,
              element: <EditGroup group={group} />,
            },
            {
              path: SINGLE_GROUP_SETTINGS_SUB_ROUTES.PERMISSIONS,
              element: <SetGroupPermissions group={group} />,
            },
            ...extensionRoutes,
          ],
        },
      ]
    : [];

  const element = useRoutes(groupRoutes);

  if (error) {
    return <ErrorPage title={t("singleGroup.errorTitle")} subtitle={t("singleGroup.errorSubtitle")} error={error} />;
  }

  if (!group || isLoading) {
    return <Loading />;
  }

  const basePath = `/group/${name}`;

  return (
    <Page title={group.name}>
      <CustomQueryFlexWrappedColumns>
        <PrimaryContentColumn>{element}</PrimaryContentColumn>
        <SecondaryNavigationColumn>
          <SecondaryNavigation label={t("singleGroup.menu.navigationLabel")}>
            <NavLink
              to={`${basePath}/${SINGLE_GROUP_SUB_ROUTES.INFO}`}
              icon="fas fa-info-circle"
              label={t("singleGroup.menu.informationNavLink")}
              title={t("singleGroup.menu.informationNavLink")}
            />
            <ExtensionPoint<extensionPoints.GroupNavigation> name="group.navigation" props={extensionProps} renderAll />
            <SubNavigation
              to={`${basePath}/${SINGLE_GROUP_SUB_ROUTES.SETTINGS}/${SINGLE_GROUP_SETTINGS_SUB_ROUTES.GENERAL}`}
              label={t("singleGroup.menu.settingsNavLink")}
              title={t("singleGroup.menu.settingsNavLink")}
            >
              <EditGroupNavLink
                group={group}
                editUrl={`${basePath}/${SINGLE_GROUP_SUB_ROUTES.SETTINGS}/${SINGLE_GROUP_SETTINGS_SUB_ROUTES.GENERAL}`}
              />
              <SetPermissionsNavLink
                group={group}
                permissionsUrl={`${basePath}/${SINGLE_GROUP_SUB_ROUTES.SETTINGS}/${SINGLE_GROUP_SETTINGS_SUB_ROUTES.PERMISSIONS}`}
              />
              <ExtensionPoint<extensionPoints.GroupSetting> name="group.setting" props={extensionProps} renderAll />
            </SubNavigation>
          </SecondaryNavigation>
        </SecondaryNavigationColumn>
      </CustomQueryFlexWrappedColumns>
    </Page>
  );
};

export default SingleGroup;
