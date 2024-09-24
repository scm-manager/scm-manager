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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Namespace, Permission, Repository, RepositoryRole } from "@scm-manager/ui-types";
import { Button, ErrorNotification, Icon } from "@scm-manager/ui-components";
import DeletePermissionButton from "../components/DeletePermissionButton";
import RoleSelector from "../components/RoleSelector";
import AdvancedPermissionsDialog from "./AdvancedPermissionsDialog";
import { useUpdatePermission } from "@scm-manager/ui-api";
import findVerbsForRole from "../utils/findVerbsForRole";

const FullWidthTr = styled.tr`
  width: 100%;
`;

const VCenteredTd = styled.td`
  display: table-cell;
  vertical-align: middle !important;
`;

type Props = {
  namespaceOrRepository: Namespace | Repository;
  availableRoles: RepositoryRole[];
  availableVerbs: string[];
  permission: Permission;
};

const PermissionIcon: FC<{ permission: Permission }> = ({ permission }) => {
  const [t] = useTranslation("repos");
  if (permission.groupPermission) {
    return <Icon title={t("permission.group")} name="user-friends" />;
  } else {
    return <Icon title={t("permission.user")} name="user" />;
  }
};

const isRepository = (namespaceOrRepository: Namespace | Repository): namespaceOrRepository is Repository => {
  return (namespaceOrRepository as Repository).name !== undefined;
};

const SinglePermission: FC<Props> = ({
  namespaceOrRepository,
  availableRoles,
  availableVerbs,
  permission: defaultPermission
}) => {
  const [permission, setPermission] = useState(defaultPermission);
  const [showAdvancedDialog, setShowAdvancedDialog] = useState(false);
  const { isLoading, error, update } = useUpdatePermission(namespaceOrRepository);
  const [t] = useTranslation("repos");
  useEffect(() => {
    setPermission(defaultPermission);
  }, [defaultPermission]);

  const availableRoleNames = !!availableRoles && availableRoles.map(r => r.name);
  const readOnly = !permission._links.update;
  const selectedVerbs = permission.role ? findVerbsForRole(availableRoles, permission.role) : permission.verbs;

  const handleRoleChange = (role: string) => {
    const newPermission = {
      ...permission,
      verbs: [],
      role
    };
    setPermission(newPermission);
    update(newPermission);
  };

  const handleVerbsChange = (verbs: string[]) => {
    const newPermission = {
      ...permission,
      role: undefined,
      verbs
    };
    setPermission(newPermission);
    update(newPermission);
    setShowAdvancedDialog(false);
  };

  return (
    <FullWidthTr>
      <VCenteredTd>
        <PermissionIcon permission={permission} /> {permission.name}
        <ErrorNotification error={error} />
      </VCenteredTd>
      {readOnly ? (
        <td>{permission.role ? permission.role : t("permission.custom")}</td>
      ) : (
        <td>
          <RoleSelector
            handleRoleChange={handleRoleChange}
            availableRoles={availableRoleNames}
            role={permission.role || ""}
            loading={isLoading}
            emptyLabel={t("permission.custom")}
          />
        </td>
      )}
      <VCenteredTd>
        <Button label={t("permission.advanced-button.label")} action={() => setShowAdvancedDialog(true)} />
      </VCenteredTd>
      <VCenteredTd className="has-text-centered">
        {permission._links.delete ? (
          <DeletePermissionButton permission={permission} namespaceOrRepository={namespaceOrRepository} />
        ) : null}
        {showAdvancedDialog ? (
          <AdvancedPermissionsDialog
            readOnly={readOnly}
            availableVerbs={availableVerbs}
            selectedVerbs={selectedVerbs || []}
            onClose={() => setShowAdvancedDialog(false)}
            onSubmit={handleVerbsChange}
            entityType={isRepository(namespaceOrRepository) ? "repository" : "namespace"}
          />
        ) : null}
      </VCenteredTd>
    </FullWidthTr>
  );
};

export default SinglePermission;
