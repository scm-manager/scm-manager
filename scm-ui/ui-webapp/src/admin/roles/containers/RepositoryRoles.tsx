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
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
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

type Props = {
  baseUrl: string;
};

const RepositoryRoles: FC<Props> = ({ baseUrl }) => {
  const params = useParams();
  const page = urls.getPageFromMatch({ params });
  const { isLoading: loading, error, data: list } = useRepositoryRoles({ page: page - 1 });
  const [t] = useTranslation("admin");
  const roles = list?._embedded.repositoryRoles;
  const canAddRoles = !!list?._links.create;

  const renderPermissionsTable = () => {
    if (list && roles && roles.length > 0) {
      return (
        <>
          <PermissionRoleTable baseUrl={baseUrl} roles={roles} />
          <LinkPaginator collection={list} page={page} />
        </>
      );
    }
    return <Notification type="info">{t("repositoryRole.overview.noPermissionRoles")}</Notification>;
  };

  const renderCreateButton = () => {
    if (canAddRoles) {
      return <CreateButton label={t("repositoryRole.overview.createButton")} link={`${baseUrl}/create`} />;
    }
    return null;
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  return (
    <>
      <Title title={t("repositoryRole.title")} />
      <Subtitle subtitle={t("repositoryRole.overview.title")} />
      {renderPermissionsTable()}
      {renderCreateButton()}
    </>
  );
};

export default RepositoryRoles;
