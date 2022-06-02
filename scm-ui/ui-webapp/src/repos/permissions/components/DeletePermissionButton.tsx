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
