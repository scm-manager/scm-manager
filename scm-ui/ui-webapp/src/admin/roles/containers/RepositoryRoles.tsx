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

  if (data && data.pageTotal < page && page > 1) {
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
