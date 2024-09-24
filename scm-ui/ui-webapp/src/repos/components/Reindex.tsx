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

import { Repository } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { useReindexRepository } from "@scm-manager/ui-api";
import { Button, ErrorNotification, Level, Notification, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
};

const Reindex: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");
  const { reindex, error, isLoading, isRunning } = useReindexRepository();

  return (
    <>
      <hr />
      <ErrorNotification error={error} />
      {isRunning ? <Notification type="success">{t("reindex.started")}</Notification> : null}
      <Subtitle>{t("reindex.subtitle")}</Subtitle>
      <p>{t("reindex.description")}</p>
      <Level
        right={
          <Button
            color="warning"
            icon="sync-alt"
            className="mt-4"
            action={() => reindex(repository)}
            disabled={isLoading}
            loading={isLoading}
          >
            {t("reindex.button")}
          </Button>
        }
      />
    </>
  );
};

export default Reindex;
