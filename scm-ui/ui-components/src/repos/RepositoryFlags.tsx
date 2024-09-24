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
import classNames from "classnames";
import { Repository } from "@scm-manager/ui-types";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { TooltipLocation } from "../Tooltip";
import RepositoryFlag from "./RepositoryFlag";
import HealthCheckFailureDetail from "./HealthCheckFailureDetail";
import styled from "styled-components";

type Props = {
  repository: Repository;
  className?: string;
  tooltipLocation?: TooltipLocation;
};

const GapedContainer = styled.div`
  gap: 0.5rem;
`;

const RepositoryFlags: FC<Props> = ({ repository, className, tooltipLocation = "right" }) => {
  const [t] = useTranslation("repos");
  const [showHealthCheck, setShowHealthCheck] = useState(false);

  const repositoryFlags = [];
  if (repository.archived) {
    repositoryFlags.push(
      <RepositoryFlag key="archived" title={t("archive.tooltip")} tooltipLocation={tooltipLocation}>
        {t("repository.archived")}
      </RepositoryFlag>
    );
  }

  if (repository.exporting) {
    repositoryFlags.push(
      <RepositoryFlag key="exporting" title={t("exporting.tooltip")} tooltipLocation={tooltipLocation}>
        {t("repository.exporting")}
      </RepositoryFlag>
    );
  }

  if (repository.healthCheckFailures && repository.healthCheckFailures.length > 0) {
    repositoryFlags.push(
      <RepositoryFlag
        key="healthcheck"
        color="danger"
        title={t("healthCheckFailure.tooltip")}
        onClick={() => setShowHealthCheck(true)}
        tooltipLocation={tooltipLocation}
      >
        {t("repository.healthCheckFailure")}
      </RepositoryFlag>
    );
  }
  const modal = (
    <HealthCheckFailureDetail
      closeFunction={() => setShowHealthCheck(false)}
      active={showHealthCheck}
      failures={repository.healthCheckFailures}
    />
  );

  return (
    <GapedContainer className={classNames("is-flex", "is-align-items-center", "is-flex-wrap-wrap", className)}>
      {modal}
      {repositoryFlags}
      <ExtensionPoint<extensionPoints.RepositoryFlags>
        name="repository.flags"
        props={{ repository, tooltipLocation }}
        renderAll={true}
      />
    </GapedContainer>
  );
};

export default RepositoryFlags;
