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
import { useAvailablePermissions, usePermissions } from "@scm-manager/ui-api";
import { ErrorPage, Loading, Subtitle } from "@scm-manager/ui-components";
import { useDocumentTitle } from "@scm-manager/ui-core";
import { Namespace, Repository } from "@scm-manager/ui-types";
import CreatePermissionForm from "./CreatePermissionForm";
import PermissionsTable from "../components/PermissionsTable";

type Props = {
  namespaceOrRepository: Namespace | Repository;
};

const isRepository = (namespaceOrRepository: Namespace | Repository): namespaceOrRepository is Repository => {
  return (namespaceOrRepository as Repository).name !== undefined;
};

const usePermissionData = (namespaceOrRepository: Namespace | Repository) => {
  const permissions = usePermissions(namespaceOrRepository);
  const availablePermissions = useAvailablePermissions();
  return {
    isLoading: permissions.isLoading || availablePermissions.isLoading,
    error: permissions.error || availablePermissions.error,
    permissions: permissions.data,
    availablePermissions: availablePermissions.data,
  };
};

const Permissions: FC<Props> = ({ namespaceOrRepository }) => {
  const { isLoading, error, permissions, availablePermissions } = usePermissionData(namespaceOrRepository);
  const [t] = useTranslation("repos");
  useDocumentTitle(
    t("repositoryRoot.menu.permissionsNavLink"),
    namespaceOrRepository.namespace + (isRepository(namespaceOrRepository) ? "/" + namespaceOrRepository.name : "")
  );

  if (error) {
    return <ErrorPage title={t("permission.error-title")} subtitle={t("permission.error-subtitle")} error={error} />;
  }

  if (isLoading || !permissions || !availablePermissions) {
    return <Loading />;
  }

  const helpText = isRepository(namespaceOrRepository) ? null : <div>{t("permission.namespace-help")}</div>;

  return (
    <div>
      <Subtitle subtitle={t("permission.title")} />
      {helpText}
      <PermissionsTable
        availableRoles={availablePermissions.repositoryRoles}
        availableVerbs={availablePermissions.repositoryVerbs}
        permissions={permissions}
        namespaceOrRepository={namespaceOrRepository}
      />
      {permissions?._links.create ? (
        <CreatePermissionForm
          availableRoles={availablePermissions.repositoryRoles}
          availableVerbs={availablePermissions.repositoryVerbs}
          currentPermissions={permissions}
          namespaceOrRepository={namespaceOrRepository}
        />
      ) : null}
    </div>
  );
};

export default Permissions;
