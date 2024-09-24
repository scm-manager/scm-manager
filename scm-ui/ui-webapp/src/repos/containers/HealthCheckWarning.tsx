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
import { Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { HealthCheckFailureDetail } from "@scm-manager/ui-components";

type Props = {
  repository: Repository;
};

const HealthCheckWarning: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");
  const [showHealthCheck, setShowHealthCheck] = useState(false);

  if (repository.healthCheckFailures?.length === 0) {
    return null;
  }

  const modal = (
    <HealthCheckFailureDetail
      closeFunction={() => setShowHealthCheck(false)}
      active={showHealthCheck}
      failures={repository.healthCheckFailures}
    />
  );

  return (
    <Notification type="danger">
      {modal}
      <div className="is-clickable" onClick={() => setShowHealthCheck(true)}>
        <div>{t("repositoryForm.healthCheckWarning.title")}</div>
        <div className="is-small">{t("repositoryForm.healthCheckWarning.subtitle")}</div>
      </div>
    </Notification>
  );
};

export default HealthCheckWarning;
