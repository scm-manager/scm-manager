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
import { User } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useDeleteUser } from "@scm-manager/ui-api";

type Props = {
  user: User;
  confirmDialog?: boolean;
};

const DeleteUser: FC<Props> = ({ confirmDialog = true, user }) => {
  const { isDeleted, isLoading, error, remove } = useDeleteUser();
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("users");
  const isDeletable = !!user._links.delete;

  const deleteUserCallback = () => remove(user);

  const confirmDelete = () => setShowConfirmAlert(true);

  const action = confirmDialog ? confirmDelete : deleteUserCallback;

  if (isDeleted) {
    return <Redirect to="/users/" />;
  }

  if (!isDeletable) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("deleteUser.confirmAlert.title")}
        message={t("deleteUser.confirmAlert.message")}
        buttons={[
          {
            label: t("deleteUser.confirmAlert.submit"),
            onClick: deleteUserCallback
          },
          {
            className: "is-info",
            label: t("deleteUser.confirmAlert.cancel"),
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
      <ErrorNotification error={error || undefined} />
      <Level right={<DeleteButton label={t("deleteUser.button")} action={action} loading={isLoading} />} />
    </>
  );
};

export default DeleteUser;
