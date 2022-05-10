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

const SinglePermission: FC<Props> = ({
  namespaceOrRepository,
  availableRoles,
  availableVerbs,
  permission: defaultPermission,
}) => {
  const [permission, setPermission] = useState(defaultPermission);
  const [showAdvancedDialog, setShowAdvancedDialog] = useState(false);
  const { isLoading, error, update } = useUpdatePermission(namespaceOrRepository);
  const [t] = useTranslation("repos");
  useEffect(() => {
    setPermission(defaultPermission);
  }, [defaultPermission]);

  const availableRoleNames = !!availableRoles && availableRoles.map((r) => r.name);
  const readOnly = !permission._links.update;
  const selectedVerbs = permission.role ? findVerbsForRole(availableRoles, permission.role) : permission.verbs;

  const handleRoleChange = (role: string) => {
    const newPermission = {
      ...permission,
      verbs: [],
      role,
    };
    setPermission(newPermission);
    update(newPermission);
  };

  const handleVerbsChange = (verbs: string[]) => {
    const newPermission = {
      ...permission,
      role: undefined,
      verbs,
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
          />
        ) : null}
      </VCenteredTd>
    </FullWidthTr>
  );
};

export default SinglePermission;
