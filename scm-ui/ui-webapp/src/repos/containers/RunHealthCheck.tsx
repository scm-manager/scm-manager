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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { Button, ErrorNotification, Level, Subtitle } from "@scm-manager/ui-components";
import { useRunHealthCheck } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
};

const RunHealthCheck: FC<Props> = ({ repository }) => {
  const { isLoading, error, runHealthCheck } = useRunHealthCheck();
  const [t] = useTranslation("repos");

  const runHealthCheckCallback = () => {
    runHealthCheck(repository);
  };

  return (
    <>
      <hr />
      <ErrorNotification error={error} />
      <Subtitle>{t("runHealthCheck.subtitle")}</Subtitle>
      <p>
        {repository.healthCheckRunning
          ? t("runHealthCheck.descriptionRunning")
          : t("runHealthCheck.descriptionNotRunning")}
      </p>
      <Level
        right={
          <Button
            className="mt-4"
            color="warning"
            icon="heartbeat"
            label={t("runHealthCheck.button")}
            action={runHealthCheckCallback}
            loading={isLoading || repository.healthCheckRunning}
          />
        }
      />
    </>
  );
};

export default RunHealthCheck;
