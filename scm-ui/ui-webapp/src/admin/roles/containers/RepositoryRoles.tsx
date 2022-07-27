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
import { Redirect, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { RepositoryRoleCollection } from "@scm-manager/ui-types";
import {
  CreateButton,
  ErrorNotification,
  LinkPaginator,
  Loading,
  Notification,
  Subtitle,
  Title,
  urls,
} from "@scm-manager/ui-components";
import PermissionRoleTable from "../components/PermissionRoleTable";
import { useRepositoryRoles } from "@scm-manager/ui-api";

type RepositoryRolesPageProps = {
  data?: RepositoryRoleCollection;
  page: number;
  baseUrl: string;
};

const RepositoryRolesPage: FC<RepositoryRolesPageProps> = ({ data, page, baseUrl }) => {
  const [t] = useTranslation("users");
  const roles = data?._embedded?.repositoryRoles;

  if (!data || !roles || roles.length === 0) {
    return <Notification type="info">{t("repositoryRole.overview.noPermissionRoles")}</Notification>;
  }

  return (
    <>
      <PermissionRoleTable baseUrl={baseUrl} roles={roles} />
      <LinkPaginator collection={data} page={page} />
    </>
  );
};

type Props = {
  baseUrl: string;
};

const RepositoryRoles: FC<Props> = ({ baseUrl }) => {
  const params = useParams();
  const page = urls.getPageFromMatch({ params });
  const { isLoading: loading, error, data } = useRepositoryRoles({ page: page - 1 });
  const [t] = useTranslation("admin");
  const canAddRoles = !!data?._links.create;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  if (data && data.pageTotal < page) {
    return <Redirect to={`${baseUrl}/${data.pageTotal}`} />;
  }

  return (
    <>
      <Title title={t("repositoryRole.title")} />
      <Subtitle subtitle={t("repositoryRole.overview.title")} />
      <RepositoryRolesPage data={data} page={page} baseUrl={baseUrl} />
      {canAddRoles ? (
        <CreateButton label={t("repositoryRole.overview.createButton")} link={`${baseUrl}/create`} />
      ) : null}
    </>
  );
};

export default RepositoryRoles;
