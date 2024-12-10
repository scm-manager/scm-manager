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
import { ErrorNotification, Loading, Subtitle, Title } from "@scm-manager/ui-components";
import RepositoryRoleForm from "./RepositoryRoleForm";
import { useCreateRepositoryRole } from "@scm-manager/ui-api";
import { Redirect } from "react-router-dom";
import { useDocumentTitle } from "@scm-manager/ui-core";

const CreateRepositoryRole: FC = () => {
  const [t] = useTranslation("admin");
  useDocumentTitle(t("repositoryRole.createSubtitle"));
  const { error, isLoading: loading, create, repositoryRole: created } = useCreateRepositoryRole();

  if (created) {
    return <Redirect to={`/admin/role/${created.name}/info`} />;
  }

  if (loading) {
    return <Loading />;
  } else if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <>
      <Title title={t("repositoryRole.title")} />
      <Subtitle subtitle={t("repositoryRole.createSubtitle")} />
      <RepositoryRoleForm submitForm={create} />
    </>
  );
};

export default CreateRepositoryRole;
