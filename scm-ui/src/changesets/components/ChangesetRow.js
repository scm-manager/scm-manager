//@flow
import React from "react";
import type {Changeset} from "@scm-manager/ui-types";
import classNames from "classnames";
import {Link} from "react-router-dom";
import ChangesetAvatar from "./ChangesetAvatar";
import injectSheet from "react-jss";

const styles = {
  pointer: {
    cursor: "pointer"
  },
  changesetGroup: {
    marginBottom: "1em"
  }
};

type Props = {
  changeset: Changeset,
  classes: any
};

class ChangesetRow extends React.Component<Props> {
  createLink = (changeset: Changeset) => {
    return `/repo/changeset/${changeset.id}`;
  };

  render() {
    const { changeset, classes } = this.props;
    const changesetLink = this.createLink(changeset);
    // todo: i18n
    return (
      <div className={classNames("box", "box-link-shadow", classes.outer)}>
        <Link className={classes.overlay} to={changesetLink} />
        <article className={classNames("media", classes.inner)}>
          <figure className="media-left">
            <ChangesetAvatar changeset={changeset} />
          </figure>
          <div className="media-content">
            <div className="content">
              <p>
                <p className="is-size-7">
                  Changeset {changeset.id} commited at {changeset.date}
                </p>
                <p className="is-size-7">
                  {changeset.author.name}{" "}
                  <a href={"mailto:" + changeset.author.mail}>
                    &lt;
                    {changeset.author.mail}
                    &gt;
                  </a>
                </p>
                <p>{changeset.description}</p>
              </p>
            </div>
          </div>
        </article>
      </div>
    );
  }
}

export default injectSheet(styles)(ChangesetRow);
