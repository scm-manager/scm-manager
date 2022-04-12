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
    url: escapedUrl
  };

  return (
    <>
      <Title title={t("repositoryRole.title")} />
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
