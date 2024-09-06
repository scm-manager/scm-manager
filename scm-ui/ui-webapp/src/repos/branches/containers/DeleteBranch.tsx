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
import { Branch, Repository } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useDeleteBranch } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  branch: Branch;
};

const DeleteBranch: FC<Props> = ({ repository, branch }: Props) => {
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");
  const { isLoading, error, remove, isDeleted } = useDeleteBranch(repository);

  if (isDeleted) {
    return <Redirect to={`/repo/${repository.namespace}/${repository.name}/branches/`} />;
  }

  if (!branch._links.delete) {
    return null;
  }

  let confirmAlert = null;
  if (showConfirmAlert) {
    confirmAlert = (
      <ConfirmAlert
        title={t("branch.delete.confirmAlert.title")}
        message={t("branch.delete.confirmAlert.message", { branch: branch.name })}
        buttons={[
          {
            label: t("branch.delete.confirmAlert.submit"),
            onClick: () => remove(branch),
            isLoading
          },
          {
            className: "is-info",
            label: t("branch.delete.confirmAlert.cancel"),
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
      <ErrorNotification error={error} />
      {showConfirmAlert && confirmAlert}
      <Level
        left={
          <div>
            <h4 className="has-text-weight-bold">{t("branch.delete.subtitle")}</h4>
            <p>{t("branch.delete.description")}</p>
          </div>
        }
        right={<DeleteButton label={t("branch.delete.button")} action={() => setShowConfirmAlert(true)} />}
      />
    </>
  );
};

export default DeleteBranch;
