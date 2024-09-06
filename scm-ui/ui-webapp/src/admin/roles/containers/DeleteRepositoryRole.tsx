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
            label: t("repositoryRole.delete.confirmAlert.submit"),
            onClick: deleteRoleCallback
          },
          {
            className: "is-info",
            label: t("repositoryRole.delete.confirmAlert.cancel"),
            onClick: () => null,
            autofocus: true
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
