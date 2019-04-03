//@flow
import React from "react";
import { Link } from "react-router-dom";
import injectSheet from "react-jss";
import type { Repository } from "@scm-manager/ui-types";
import { DateFromNow } from "@scm-manager/ui-components";
import RepositoryEntryLink from "./RepositoryEntryLink";
import classNames from "classnames";
import RepositoryAvatar from "./RepositoryAvatar";

const styles = {
  inner: {
    position: "relative",
    pointerEvents: "none",
    zIndex: 1
  },
  innerLink: {
    pointerEvents: "all"
  },
  centerImage: {
    marginTop: "0.8em",
    marginLeft: "1em !important"
  }
};

type Props = {
  repository: Repository,
  fullColumnWidth?: boolean,
  // context props
  classes: any
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

  render() {
    const { repository, classes, fullColumnWidth } = this.props;
    const repositoryLink = this.createLink(repository);
    const halfColumn = fullColumnWidth ? "is-full" : "is-half";
    const overlayLinkClass = fullColumnWidth
      ? "overlay-full-column"
      : "overlay-half-column";
    return (
      <div
        className={classNames(
          "box",
          "box-link-shadow",
          "column",
          "is-clipped",
          halfColumn
        )}
      >
        <Link className={classNames(overlayLinkClass)} to={repositoryLink} />
        <article className={classNames("media", classes.inner)}>
          <figure className={classNames(classes.centerImage, "media-left")}>
            <RepositoryAvatar repository={repository} />
          </figure>
          <div className={classNames("media-content", "text-box")}>
            <div className="content">
              <p className="is-marginless">
                <strong>{repository.name}</strong>
              </p>
              <p className={"shorten-text"}>{repository.description}</p>
            </div>
            <nav className="level is-mobile">
              <div className="level-left">
                {this.renderBranchesLink(repository, repositoryLink)}
                {this.renderChangesetsLink(repository, repositoryLink)}
                {this.renderSourcesLink(repository, repositoryLink)}
                {this.renderModifyLink(repository, repositoryLink)}
              </div>
              <div className="level-right is-hidden-mobile">
                <small className="level-item">
                  <DateFromNow date={repository.creationDate} />
                </small>
              </div>
            </nav>
          </div>
        </article>
      </div>
    );
  }
}

export default injectSheet(styles)(RepositoryEntry);
