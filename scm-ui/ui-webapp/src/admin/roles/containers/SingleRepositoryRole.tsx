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
import { Route, useParams, useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { ErrorPage, Loading, Title, urls } from "@scm-manager/ui-components";
import PermissionRoleDetail from "../components/PermissionRoleDetails";
import EditRepositoryRole from "./EditRepositoryRole";
import { useRepositoryRole } from "@scm-manager/ui-api";

const SingleRepositoryRole: FC = () => {
  const [t] = useTranslation("admin");
  const match = useRouteMatch();
  const { role: roleName } = useParams<{ role: string }>();
  const { data: role, error, isLoading: loading } = useRepositoryRole(roleName);

  if (error) {
    return (
      <ErrorPage title={t("repositoryRole.errorTitle")} subtitle={t("repositoryRole.errorSubtitle")} error={error} />
    );
  }

  if (!role || loading) {
    return <Loading />;
  }

  const url = urls.matchedUrlFromMatch(match);
  const escapedUrl = urls.escapeUrlForRoute(url);

  const extensionProps = {
    role,
    url: escapedUrl,
  };

  return (
    <>
      <Route path={`${escapedUrl}/info`}>
        <PermissionRoleDetail role={role} url={url} />
      </Route>
      <Route path={`${escapedUrl}/edit`} exact>
        <EditRepositoryRole role={role} />
      </Route>
      <ExtensionPoint<extensionPoints.RolesRoute> name="roles.route" props={extensionProps} renderAll={true} />
    </>
  );
};

export default SingleRepositoryRole;
