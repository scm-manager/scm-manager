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
import RepositoryEntryLink from "./RepositoryEntryLink";
import RepositoryAvatar from "./RepositoryAvatar";
import { ExtensionPoint } from "@scm-manager/ui-extensions";

type DateProp = Date | string;

type Props = {
  repository: Repository;
  // @VisibleForTesting
  // the baseDate is only to avoid failing snapshot tests
  baseDate?: DateProp;
};

class RepositoryEntry extends React.Component<Props> {
  createLink = (repository: Repository) => {
    return `/repo/${repository.namespace}/${repository.name}`;
  };

  renderBranchesLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["branches"]) {
      return <RepositoryEntryLink icon="code-branch" to={repositoryLink + "/branches/"} />;
    }
    return null;
  };

  renderChangesetsLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["changesets"]) {
      return <RepositoryEntryLink icon="exchange-alt" to={repositoryLink + "/code/changesets/"} />;
    }
    return null;
  };

  renderSourcesLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["sources"]) {
      return <RepositoryEntryLink icon="code" to={repositoryLink + "/code/sources/"} />;
    }
    return null;
  };

  renderModifyLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["update"]) {
      return <RepositoryEntryLink icon="cog" to={repositoryLink + "/settings/general"} />;
    }
    return null;
  };

  createFooterLeft = (repository: Repository, repositoryLink: string) => {
    return (
      <>
        {this.renderBranchesLink(repository, repositoryLink)}
        {this.renderChangesetsLink(repository, repositoryLink)}
        {this.renderSourcesLink(repository, repositoryLink)}
        <ExtensionPoint name={"repository.card.quickLink"} props={{ repository, repositoryLink }} renderAll={true} />
        {this.renderModifyLink(repository, repositoryLink)}
      </>
    );
  };

  createFooterRight = (repository: Repository, baseDate?: DateProp) => {
    return (
      <small className="level-item">
        <DateFromNow baseDate={baseDate} date={repository.creationDate} />
      </small>
    );
  };

  createTitle = () => {
    const { repository } = this.props;
    return (
      <>
        <ExtensionPoint name="repository.card.beforeTitle" props={{ repository }} /> <strong>{repository.name}</strong>
      </>
    );
  };

  render() {
    const { repository, baseDate } = this.props;
    const repositoryLink = this.createLink(repository);
    const footerLeft = this.createFooterLeft(repository, repositoryLink);
    const footerRight = this.createFooterRight(repository, baseDate);
    const title = this.createTitle();
    return (
      <CardColumn
        avatar={<RepositoryAvatar repository={repository} />}
        title={title}
        description={repository.description}
        link={repositoryLink}
        footerLeft={footerLeft}
        footerRight={footerRight}
      />
    );
  }
}

export default RepositoryEntry;
