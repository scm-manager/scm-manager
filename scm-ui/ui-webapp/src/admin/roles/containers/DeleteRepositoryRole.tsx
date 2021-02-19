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
import { Redirect } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { RepositoryRole } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useDeleteRepositoryRole } from "@scm-manager/ui-api";

type Props = {
  role: RepositoryRole;
  confirmDialog?: boolean;
};

const DeleteRepositoryRole: FC<Props> = ({ confirmDialog = true, role }: Props) => {
  const { isLoading: loading, error, remove, isDeleted } = useDeleteRepositoryRole();
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("admin");

  if (isDeleted) {
    return <Redirect to="/admin/roles" />;
  }

  const deleteRoleCallback = () => remove(role);

  const confirmDelete = () => {
    setShowConfirmAlert(true);
  };

  const isDeletable = () => {
    return role._links.delete;
  };

  const action = confirmDialog ? confirmDelete : deleteRoleCallback;

  if (!isDeletable()) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("repositoryRole.delete.confirmAlert.title")}
        message={t("repositoryRole.delete.confirmAlert.message")}
        buttons={[
          {
            className: "is-outlined",
            label: t("repositoryRole.delete.confirmAlert.submit"),
            onClick: deleteRoleCallback
          },
          {
            label: t("repositoryRole.delete.confirmAlert.cancel"),
            onClick: () => null
          }
        ]}
        close={() => setShowConfirmAlert(false)}
      />
    );
  }

  return (
    <>
      <hr />
      <ErrorNotification error={error} />
      <Level right={<DeleteButton label={t("repositoryRole.delete.button")} action={action} loading={loading} />} />
    </>
  );
};

export default DeleteRepositoryRole;
