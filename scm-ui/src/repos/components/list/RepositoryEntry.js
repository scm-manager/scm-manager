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
  overlay: {
    position: "absolute",
    height: "calc(120px - 1.5rem)",
    width: "calc(50% - 3rem)"
  },
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
  // context props
  classes: any
};

class RepositoryEntry extends React.Component<Props> {
  createLink = (repository: Repository) => {
    return `/repo/${repository.namespace}/${repository.name}`;
  };

  renderChangesetsLink = (repository: Repository, repositoryLink: string) => {
    if (repository._links["changesets"]) {
      return (
        <RepositoryEntryLink
          iconClass="fa-code-branch fa-lg"
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
          to={repositoryLink + "/edit"}
        />
      );
    }
    return null;
  };

  render() {
    const { repository, classes } = this.props;
    const repositoryLink = this.createLink(repository);

    return (
      <div
        className={classNames(
          "box",
          "box-link-shadow",
          "column is-clipped",
          "is-half"
        )}
      >
        <Link className={classNames(classes.overlay)} to={repositoryLink} />
        <article className={classNames("media", classes.inner)}>
          <figure className={classNames(classes.centerImage, "media-left")}>
            <RepositoryAvatar repository={repository} />
          </figure>
          <div className="media-content">
            <div className="content">
              <p className="is-marginless">
                <strong>{repository.name}</strong>
              </p>
              <p className={"shorten-text"}>{repository.description}</p>
            </div>
            <nav className="level is-mobile">
              <div className="level-left">
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
