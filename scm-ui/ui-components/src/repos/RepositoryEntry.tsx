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
import React from "react";
import { Repository } from "@scm-manager/ui-types";
import { CardColumn, DateFromNow } from "@scm-manager/ui-components";
import RepositoryAvatar from "./RepositoryAvatar";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { withTranslation, WithTranslation } from "react-i18next";
import styled from "styled-components";
import HealthCheckFailureDetail from "./HealthCheckFailureDetail";
import RepositoryFlag from "./RepositoryFlag";

type DateProp = Date | string;

type Props = WithTranslation & {
  repository: Repository;
  // @VisibleForTesting
  // the baseDate is only to avoid failing snapshot tests
  baseDate?: DateProp;
};

type State = {
  showHealthCheck: boolean;
};

const Title = styled.span`
  display: flex;
  align-items: center;
`;

const RepositoryFlagContainer = styled.div`
  .tag {
    margin-left: 0.25rem;
  }
`;

class RepositoryEntry extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      showHealthCheck: false,
    };
  }

  createLink = (repository: Repository) => {
    return `/repo/${repository.namespace}/${repository.name}`;
  };

  createFooterRight = (repository: Repository, baseDate?: DateProp) => {
    return (
      <small className="level-item">
        <DateFromNow baseDate={baseDate} date={repository.lastModified || repository.creationDate} />
      </small>
    );
  };

  createTitle = () => {
    const { repository, t } = this.props;
    const repositoryFlags = [];
    if (repository.archived) {
      repositoryFlags.push(<RepositoryFlag title={t("archive.tooltip")}>{t("repository.archived")}</RepositoryFlag>);
    }

    if (repository.exporting) {
      repositoryFlags.push(<RepositoryFlag title={t("exporting.tooltip")}>{t("repository.exporting")}</RepositoryFlag>);
    }

    if (repository.healthCheckFailures && repository.healthCheckFailures.length > 0) {
      repositoryFlags.push(
        <RepositoryFlag
          color="danger"
          title={t("healthCheckFailure.tooltip")}
          onClick={() => {
            this.setState({ showHealthCheck: true });
          }}
        >
          {t("repository.healthCheckFailure")}
        </RepositoryFlag>
      );
    }

    return (
      <Title>
        <ExtensionPoint name="repository.card.beforeTitle" props={{ repository }} />
        <strong>{repository.name}</strong>{" "}
        <RepositoryFlagContainer>
          {repositoryFlags}
          <ExtensionPoint name="repository.flags" props={{ repository }} renderAll={true} />
        </RepositoryFlagContainer>
      </Title>
    );
  };

  render() {
    const { repository, baseDate } = this.props;
    const repositoryLink = this.createLink(repository);
    const footerRight = this.createFooterRight(repository, baseDate);
    const title = this.createTitle();
    const modal = (
      <HealthCheckFailureDetail
        closeFunction={() => this.setState({ showHealthCheck: false })}
        active={this.state.showHealthCheck}
        failures={repository.healthCheckFailures}
      />
    );

    return (
      <>
        {modal}
        <CardColumn
          avatar={<RepositoryAvatar repository={repository} />}
          title={title}
          description={repository.description}
          link={repositoryLink}
          footerLeft={undefined}
          footerRight={footerRight}
        />
      </>
    );
  }
}

export default withTranslation("repos")(RepositoryEntry);
