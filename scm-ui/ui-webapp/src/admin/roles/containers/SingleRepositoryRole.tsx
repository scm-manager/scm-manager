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
import { Navigate, RouteObject, useParams, useRoutes } from "react-router-dom";
import { useRepositoryRole } from "@scm-manager/ui-api";
import { ErrorPage } from "@scm-manager/ui-components";
import { ADMIN_SINGLE_ROLE_SUB_ROUTES, Loading } from "@scm-manager/ui-core";
import { extensionPoints, useRouteExtensions } from "@scm-manager/ui-extensions";
import PermissionRoleDetail from "../components/PermissionRoleDetails";
import EditRepositoryRole from "./EditRepositoryRole";

const SingleRepositoryRole: FC = () => {
  const [t] = useTranslation("admin");
  const { role: roleName } = useParams<{ role: string }>();
  const { data: role, error, isLoading: loading } = useRepositoryRole(roleName!);

  const extensionProps = {
    role: role!,
  };

  const rolesExtensionRoutes = useRouteExtensions<extensionPoints.RolesRoute>({
    name: "roles.route",
    props: extensionProps,
  });

  const singleRepositoryRoleRoutes: RouteObject[] = role
    ? [
        {
          index: true,
          element: <Navigate to={ADMIN_SINGLE_ROLE_SUB_ROUTES.INFO} replace />,
        },
        {
          path: ADMIN_SINGLE_ROLE_SUB_ROUTES.INFO,
          element: <PermissionRoleDetail role={role} />,
        },
        {
          path: ADMIN_SINGLE_ROLE_SUB_ROUTES.EDIT,
          element: <EditRepositoryRole role={role} />,
        },
        ...rolesExtensionRoutes,
      ]
    : [];

  const element = useRoutes(singleRepositoryRoleRoutes);

  if (error) {
    return (
      <ErrorPage title={t("repositoryRole.errorTitle")} subtitle={t("repositoryRole.errorSubtitle")} error={error} />
    );
  }

  if (!role || loading) {
    return <Loading />;
  }

  return <>{element}</>;
};

export default SingleRepositoryRole;
