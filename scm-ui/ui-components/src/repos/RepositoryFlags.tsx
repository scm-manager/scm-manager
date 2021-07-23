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
import RepositoryFlag from "./RepositoryFlag";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Repository } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import HealthCheckFailureDetail from "./HealthCheckFailureDetail";

type Props = {
  repository: Repository;
  className?: string;
};

const Wrapper = styled.span`
  display: flex;
  align-items: center;
`;

const RepositoryFlagContainer = styled.div`
  .tag {
    margin-left: 0.25rem;
  }
`;

const RepositoryFlags: FC<Props> = ({ repository, className }) => {
  const [t] = useTranslation("repos");
  const [showHealthCheck, setShowHealthCheck] = useState(false);

  const repositoryFlags = [];
  if (repository.archived) {
    repositoryFlags.push(
      <RepositoryFlag key="archived" title={t("archive.tooltip")}>
        {t("repository.archived")}
      </RepositoryFlag>
    );
  }

  if (repository.exporting) {
    repositoryFlags.push(
      <RepositoryFlag key="exporting" title={t("exporting.tooltip")}>
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
    <Wrapper>
      {modal}
      <RepositoryFlagContainer>
        {repositoryFlags}
        <ExtensionPoint name="repository.flags" props={{ repository }} renderAll={true} />
      </RepositoryFlagContainer>
    </Wrapper>
  );
};

export default RepositoryFlags;
