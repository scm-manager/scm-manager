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
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { Button, ConfirmAlert, ErrorNotification, Level } from "@scm-manager/ui-components";
import { useUnarchiveRepository } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  confirmDialog?: boolean;
};

const UnarchiveRepo: FC<Props> = ({ repository, confirmDialog = true }) => {
  const { isLoading, error, unarchive } = useUnarchiveRepository();
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");

  const unarchiveRepoCallback = () => {
    unarchive(repository);
  };

  const confirmUnarchive = () => {
    setShowConfirmAlert(true);
  };

  const action = confirmDialog ? confirmUnarchive : unarchiveRepoCallback;

  const confirmAlert = (
    <ConfirmAlert
      title={t("unarchiveRepo.confirmAlert.title")}
      message={t("unarchiveRepo.confirmAlert.message")}
      buttons={[
        {
          label: t("unarchiveRepo.confirmAlert.submit"),
          isLoading,
          onClick: () => unarchiveRepoCallback(),
          autofocus: true
        },
        {
          className: "is-info",
          label: t("unarchiveRepo.confirmAlert.cancel"),
          onClick: () => null
        }
      ]}
      close={() => setShowConfirmAlert(false)}
    />
  );

  return (
    <>
      <ErrorNotification error={error} />
      {showConfirmAlert && confirmAlert}
      <Level
        left={
          <div>
            <h4 className="has-text-weight-bold">{t("unarchiveRepo.subtitle")}</h4>
            <p>{t("unarchiveRepo.description")}</p>
          </div>
        }
        right={
          <Button
            color="warning"
            icon="box-open"
            label={t("unarchiveRepo.button")}
            action={action}
            loading={isLoading}
          />
        }
      />
    </>
  );
};

export default UnarchiveRepo;
