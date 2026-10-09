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
import { Navigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Group } from "@scm-manager/ui-types";
import { ConfirmAlert, ErrorNotification } from "@scm-manager/ui-components";
import { Button, Icon, Level } from "@scm-manager/ui-core";
import { useDeleteGroup } from "@scm-manager/ui-api";

type Props = {
  group: Group;
  confirmDialog?: boolean;
};

export const DeleteGroup: FC<Props> = ({ confirmDialog = true, group }) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const { isPending, error, remove, isDeleted } = useDeleteGroup();
  const [t] = useTranslation("groups");

  if (isDeleted) {
    return <Navigate to="/groups/" />;
  }

  const deleteGroupCallback = () => remove(group);

  const confirmDelete = () => setShowConfirmAlert(true);

  const isDeletable = () => group._links.delete;
  const action = confirmDialog ? confirmDelete : deleteGroupCallback;

  if (!isDeletable()) {
    return null;
  }

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("deleteGroup.confirmAlert.title")}
        message={t("deleteGroup.confirmAlert.message")}
        buttons={[
          {
            label: t("deleteGroup.confirmAlert.submit"),
            onClick: deleteGroupCallback,
          },
          {
            className: "is-info",
            label: t("deleteGroup.confirmAlert.cancel"),
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
      <hr />
      <ErrorNotification error={error || undefined} />
      <Level
        right={
          <Button variant="signal" onClick={action} isLoading={isPending}>
            <Icon>times</Icon>
            <span>{t("deleteGroup.button")}</span>
          </Button>
        }
      />
    </>
  );
};

export default DeleteGroup;
