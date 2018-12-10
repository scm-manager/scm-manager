//@flow
import React from "react";
import type { Changeset, Repository, Tag } from "@scm-manager/ui-types";

import classNames from "classnames";
import {Interpolate, translate} from "react-i18next";
import ChangesetId from "./ChangesetId";
import injectSheet from "react-jss";
import {DateFromNow} from "../..";
import ChangesetAuthor from "./ChangesetAuthor";
import ChangesetTag from "./ChangesetTag";

import {parseDescription} from "./changesets";
import {AvatarWrapper, AvatarImage} from "../../avatar";

const styles = {
  pointer: {
    cursor: "pointer"
  },
  changesetGroup: {
    marginBottom: "1em"
  },
  withOverflow: {
    overflow: "auto"
  }
};

type Props = {
  repository: Repository,
  changeset: Changeset,
  t: any,
  classes: any
};

class ChangesetRow extends React.Component<Props> {
  createLink = (changeset: Changeset) => {
    const { repository } = this.props;
    return <ChangesetId changeset={changeset} repository={repository} />;
  };

  getTags = () => {
    const { changeset } = this.props;
    return changeset._embedded.tags || [];
  };

  render() {
    const { changeset, classes } = this.props;
    const changesetLink = this.createLink(changeset);
    const dateFromNow = <DateFromNow date={changeset.date} />;
    const authorLine = <ChangesetAuthor changeset={changeset} />;
    const description = parseDescription(changeset.description);

    return (
      <article className={classNames("media", classes.inner)}>
        <AvatarWrapper>
          <div>
            <figure className="media-left">
              <p className="image is-64x64">
                <AvatarImage person={changeset.author} />
              </p>
            </figure>
          </div>
        </AvatarWrapper>
        <div className={classNames("media-content", classes.withOverflow)}>
          <div className="content">
            <p className="is-ellipsis-overflow">
              <strong>{description.title}</strong>
              <br />
              <Interpolate
                i18nKey="changesets.changeset.summary"
                id={changesetLink}
                time={dateFromNow}
              />
            </p>{" "}
            <div className="is-size-7">{authorLine}</div>
          </div>
        </div>
        {this.renderTags()}
      </article>
    );
  }

  renderTags = () => {
    const tags = this.getTags();
    if (tags.length > 0) {
      return (
        <div className="media-right">
          {tags.map((tag: Tag) => {
            return <ChangesetTag key={tag.name} tag={tag} />;
          })}
        </div>
      );
    }
    return null;
  };
}

export default injectSheet(styles)(translate("repos")(ChangesetRow));
