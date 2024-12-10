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
import RepositoryRoleForm from "./RepositoryRoleForm";
import { useTranslation } from "react-i18next";
import { ErrorNotification, Loading, Subtitle, Title } from "@scm-manager/ui-components";
import { RepositoryRole } from "@scm-manager/ui-types";
import DeleteRepositoryRole from "./DeleteRepositoryRole";
import { useUpdateRepositoryRole } from "@scm-manager/ui-api";
import { Redirect } from "react-router-dom";
import { useDocumentTitle } from "@scm-manager/ui-core";

type Props = {
  role: RepositoryRole;
};

const EditRepositoryRole: FC<Props> = ({ role }) => {
  const [t] = useTranslation("admin");
  useDocumentTitle(t("repositoryRole.editSubtitle"));
  const { isUpdated, update, error, isLoading: loading } = useUpdateRepositoryRole();

  if (isUpdated) {
    return <Redirect to="/admin/roles/" />;
  }

  if (loading) {
    return <Loading />;
  } else if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <>
      <Title title={t("repositoryRole.detailsTitle")} />
      <Subtitle subtitle={t("repositoryRole.editSubtitle")} />
      <RepositoryRoleForm role={role} submitForm={update} />
      <DeleteRepositoryRole role={role} />
    </>
  );
};

export default EditRepositoryRole;
