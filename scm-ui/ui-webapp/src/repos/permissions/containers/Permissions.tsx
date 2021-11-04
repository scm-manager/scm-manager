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
import { useTranslation } from "react-i18next";
import { ErrorPage, Loading, Subtitle } from "@scm-manager/ui-components";
import { Namespace, Repository } from "@scm-manager/ui-types";
import CreatePermissionForm from "./CreatePermissionForm";
import PermissionsTable from "../components/PermissionsTable";
import { useAvailablePermissions, usePermissions } from "@scm-manager/ui-api";

type Props = {
  namespaceOrRepository: Namespace | Repository;
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

  if (error) {
    return <ErrorPage title={t("permission.error-title")} subtitle={t("permission.error-subtitle")} error={error} />;
  }

  if (isLoading || !permissions || !availablePermissions) {
    return <Loading />;
  }

  return (
    <div>
      <Subtitle subtitle={t("permission.title")} />
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
