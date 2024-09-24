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
import { useArchiveRepository } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  confirmDialog?: boolean;
};

const ArchiveRepo: FC<Props> = ({ repository, confirmDialog = true }) => {
  const { isLoading, error, archive } = useArchiveRepository();
  const [showConfirmAlert, setShowConfirmAlert] = useState(false);
  const [t] = useTranslation("repos");

  const archiveRepoCallback = () => {
    archive(repository);
  };

  const confirmArchive = () => {
    setShowConfirmAlert(true);
  };

  const action = confirmDialog ? confirmArchive : archiveRepoCallback;

  const confirmAlert = (
    <ConfirmAlert
      title={t("archiveRepo.confirmAlert.title")}
      message={t("archiveRepo.confirmAlert.message")}
      buttons={[
        {
          label: t("archiveRepo.confirmAlert.submit"),
          onClick: () => archiveRepoCallback()
        },
        {
          className: "is-info",
          label: t("archiveRepo.confirmAlert.cancel"),
          onClick: () => null,
          autofocus: true
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
            <h4 className="has-text-weight-bold">{t("archiveRepo.subtitle")}</h4>
            <p>{t("archiveRepo.description")}</p>
          </div>
        }
        right={
          <Button color="warning" icon="archive" label={t("archiveRepo.button")} action={action} loading={isLoading} />
        }
      />
    </>
  );
};

export default ArchiveRepo;
