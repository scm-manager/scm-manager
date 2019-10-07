//@flow
import React from "react";
import type { Repository } from "@scm-manager/ui-types";
import { CardColumn, DateFromNow } from "@scm-manager/ui-components";
import RepositoryEntryLink from "./RepositoryEntryLink";
import RepositoryAvatar from "./RepositoryAvatar";

type Props = {
  repository: Repository
};

class RepositoryEntry extends React.Component<Props> {
  createLink = (repository: Repository) => {
    return `/repo/${repository.namespace}/${repository.name}`;
  };

  renderBranchesLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["branches"]) {
      return (
        <RepositoryEntryLink
          iconClass="fas fa-code-branch fa-lg"
          to={repositoryLink + "/branches"}
        />
      );
    }
    return null;
  };

  renderChangesetsLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["changesets"]) {
      return (
        <RepositoryEntryLink
          iconClass="fas fa-exchange-alt fa-lg"
          to={repositoryLink + "/changesets"}
        />
      );
    }
    return null;
  };

  renderSourcesLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["sources"]) {
      return (
        <RepositoryEntryLink
          iconClass="fa-code fa-lg"
          to={repositoryLink + "/sources"}
        />
      );
    }
    return null;
  };

  renderModifyLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["update"]) {
      return (
        <RepositoryEntryLink
          iconClass="fa-cog fa-lg"
          to={repositoryLink + "/settings/general"}
        />
      );
    }
    return null;
  };

  createFooterLeft = (repository: Repository, repositoryLink: string) => {
    return (
      <>
        {this.renderBranchesLink(repository, repositoryLink)}
        {this.renderChangesetsLink(repository, repositoryLink)}
        {this.renderSourcesLink(repository, repositoryLink)}
        {this.renderModifyLink(repository, repositoryLink)}
      </>
    );
  };

  createFooterRight = (repository: Repository) => {
    return (
      <small className="level-item">
        <DateFromNow date={repository.creationDate} />
      </small>
    );
  };

  render() {
    const { repository } = this.props;
    const repositoryLink = this.createLink(repository);
    const footerLeft = this.createFooterLeft(repository, repositoryLink);
    const footerRight = this.createFooterRight(repository);
    return (
      <CardColumn
        avatar={<RepositoryAvatar repository={repository} />}
        title={repository.name}
        description={repository.description}
        link={repositoryLink}
        footerLeft={footerLeft}
        footerRight={footerRight}
      />
    );
  }
}

export default RepositoryEntry;
