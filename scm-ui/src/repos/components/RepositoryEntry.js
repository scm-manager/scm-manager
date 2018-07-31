//@flow
import React from "react";
import { Link } from "react-router-dom";
import injectSheet from "react-jss";
import type { Repository } from "../types/Repositories";
import DateFromNow from "../../components/DateFromNow";
import RepositoryEntryLink from "./RepositoryEntryLink";
import classNames from "classnames";

import icon from "../../images/blib.jpg";

// TODO we need a variable or something central for the hover

const styles = {
  outer: {
    position: "relative"
  },
  overlay: {
    position: "absolute",
    left: 0,
    top: 0,
    bottom: 0,
    right: 0
  },
  inner: {
    position: "relative",
    pointerEvents: "none",
    zIndex: 1
  },
  innerLink: {
    pointerEvents: "all"
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
          iconClass="fa-code-fork"
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
          iconClass="fa-code"
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
          iconClass="fa-cog"
          to={repositoryLink + "/modify"}
        />
      );
    }
    return null;
  };

  render() {
    const { repository, classes } = this.props;
    const repositoryLink = this.createLink(repository);
    return (
      <div className={classNames("box", "box-link-shadow", classes.outer)}>
        <Link className={classes.overlay} to={repositoryLink} />
        <article className={classNames("media", classes.inner)}>
          <figure className="media-left">
            <p className="image is-64x64">
              <img src={icon} alt="Logo" />
            </p>
          </figure>
          <div className="media-content">
            <div className="content">
              <p>
                <strong>{repository.name}</strong>
                <br />
                {repository.description}
              </p>
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
