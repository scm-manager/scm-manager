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
import React, { FC, useState } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Permission } from "@scm-manager/ui-types";
import { ConfirmAlert } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  permission: Permission;
  namespace: string;
  repoName: string;
  confirmDialog?: boolean;
  deletePermission: (permission: Permission, namespace: string, repoName: string) => void;
  loading: boolean;
};

const DeletePermissionButton: FC<Props> = ({
  confirmDialog = true,
  permission,
  namespace,
  t,
  deletePermission,
  repoName
}) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);

  const deletePermissionCallback = () => {
    deletePermission(permission, namespace, repoName);
  };

  const confirmDelete = () => {
    setShowConfirmAlert(true);
  };

  const isDeletable = () => {
    return permission._links.delete;
  };

  const action = confirmDialog ? confirmDelete : deletePermissionCallback;

  if (!isDeletable()) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("permission.delete-permission-button.confirm-alert.title")}
        message={t("permission.delete-permission-button.confirm-alert.message")}
        buttons={[
          {
            className: "is-outlined",
            label: t("permission.delete-permission-button.confirm-alert.submit"),
            onClick: () => deletePermissionCallback()
          },
          {
            label: t("permission.delete-permission-button.confirm-alert.cancel"),
            onClick: () => null
          }
        ]}
        close={() => setShowConfirmAlert(false)}
      />
    );
  }

  return (
    <a className="level-item" onClick={action}>
      <span className="icon is-small">
        <i className="fas fa-trash" />
      </span>
    </a>
  );
};

export default withTranslation("repos")(DeletePermissionButton);
