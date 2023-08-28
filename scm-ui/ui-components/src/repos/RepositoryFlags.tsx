/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
