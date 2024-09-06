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
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { ConfirmAlert, DeleteButton, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useDeleteRepository } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  confirmDialog?: boolean;
};

const DeleteRepo: FC<Props> = ({ repository, confirmDialog = true }) => {
  const history = useHistory();
  const { isLoading, error, remove, isDeleted } = useDeleteRepository({
    onSuccess: () => {
      history.push("/repos/");
    }
  });
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");
  useEffect(() => {
    if (isDeleted) {
      setShowConfirmAlert(false);
    }
  }, [isDeleted]);

  const deleteRepoCallback = () => {
    remove(repository);
  };

  const confirmDelete = () => {
    setShowConfirmAlert(true);
  };

  const action = confirmDialog ? confirmDelete : deleteRepoCallback;

  if (showConfirmAlert) {
    return (
      <ConfirmAlert
        title={t("deleteRepo.confirmAlert.title")}
        message={t("deleteRepo.confirmAlert.message")}
        buttons={[
          {
            label: t("deleteRepo.confirmAlert.submit"),
            onClick: () => deleteRepoCallback()
          },
          {
            className: "is-info",
            label: t("deleteRepo.confirmAlert.cancel"),
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
      <Level
        left={
          <div>
            <h4 className="has-text-weight-bold">{t("deleteRepo.subtitle")}</h4>
            <p>{t("deleteRepo.description")}</p>
          </div>
        }
        right={<DeleteButton label={t("deleteRepo.button")} action={action} loading={isLoading} />}
      />
    </>
  );
};

export default DeleteRepo;
