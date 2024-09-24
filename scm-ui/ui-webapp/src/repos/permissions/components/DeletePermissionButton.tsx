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
import { Namespace, Permission, Repository } from "@scm-manager/ui-types";
import { Button, ConfirmAlert, ErrorNotification } from "@scm-manager/ui-components";
import { useDeletePermission } from "@scm-manager/ui-api";

type Props = {
  permission: Permission;
  namespaceOrRepository: Namespace | Repository;
  confirmDialog?: boolean;
};

const DeletePermissionButton: FC<Props> = ({ namespaceOrRepository, permission, confirmDialog = true }) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const { isLoading, error, remove, isDeleted } = useDeletePermission(namespaceOrRepository);
  const [t] = useTranslation("repos");
  useEffect(() => {
    if (isDeleted) {
      setShowConfirmAlert(false);
    }
  }, [isDeleted]);

  const deletePermission = () => {
    remove(permission);
  };

  const confirmDelete = () => {
    setShowConfirmAlert(true);
  };

  const action = confirmDialog ? confirmDelete : deletePermission;

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("permission.delete-permission-button.confirm-alert.title")}
        message={t("permission.delete-permission-button.confirm-alert.message")}
        buttons={[
          {
            label: t("permission.delete-permission-button.confirm-alert.submit"),
            isLoading,
            onClick: () => deletePermission(),
          },
          {
            className: "is-info",
            label: t("permission.delete-permission-button.confirm-alert.cancel"),
            onClick: () => null,
            autofocus: true,
          },
        ]}
        close={() => setShowConfirmAlert(false)}
      />
    );
  }

  return (
    <>
      <ErrorNotification error={error} />
      <Button
        color="text"
        icon="trash"
        action={action}
        title={t("permission.delete-permission-button.label")}
        className="px-2"
      />
    </>
  );
};

export default DeletePermissionButton;
