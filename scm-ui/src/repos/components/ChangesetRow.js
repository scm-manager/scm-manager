//@flow
import React from "react";
import type { Changeset } from "@scm-manager/ui-types";
import classNames from "classnames";
import { translate } from "react-i18next";
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
  t: any,
  classes: any
};

class ChangesetRow extends React.Component<Props> {
  createLink = (changeset: Changeset) => {
    return `/repo/changeset/${changeset.id}`;
  };

  render() {
    const { changeset, t, classes } = this.props;
    const changesetLink = this.createLink(changeset);
    const authorLine = (
      <>
        {changeset.author.name}{" "}
        <a
          className="is-hidden-mobile"
          href={"mailto:" + changeset.author.mail}
        >
          &lt;
          {changeset.author.mail}
          &gt;
        </a>
      </>
    );
    // todo: i18n
    return (
      <article className={classNames("media", classes.inner)}>
        <figure className="media-left">
          <ChangesetAvatar changeset={changeset} />
        </figure>
        <div className="media-content">
          <div className="content">
            <p className="is-ellipsis-overflow">
              {changeset.description}
              <br />
              {t("changeset.summary", {
                id: changeset.id,
                time: changeset.date
              })}
            </p>
            <p className="is-size-7">{authorLine}</p>
          </div>
        </div>
      </article>
    );
  }
}

export default injectSheet(styles)(translate("changesets")(ChangesetRow));
