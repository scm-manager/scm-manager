//@flow
import React from "react";
import type { Changeset, Repository } from "@scm-manager/ui-types";

import classNames from "classnames";
import { Interpolate, translate } from "react-i18next";
import ChangesetId from "./ChangesetId";
import injectSheet from "react-jss";
import { DateFromNow } from "../..";
import ChangesetAuthor from "./ChangesetAuthor";
import { parseDescription } from "./changesets";
import { AvatarWrapper, AvatarImage } from "../../avatar";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import ChangesetTags from "./ChangesetTags";
import ChangesetButtonGroup from "./ChangesetButtonGroup";

const styles = {
  changeset: {
    // & references parent rule
    // have a look at https://cssinjs.org/jss-plugin-nested?v=v10.0.0-alpha.9
    "& + &": {
      borderTop: "1px solid rgba(219, 219, 219, 0.5)",
      marginTop: "1rem",
      paddingTop: "1rem"
    }
  },
  avatarFigure: {
    marginTop: ".25rem",
    marginRight: ".5rem"
  },
  avatarImage: {
    height: "35px",
    width: "35px"
  },
  metadata: {
    marginLeft: 0
  },
  isVcentered: {
    alignSelf: "center"
  },
  flexVcenter: {
    display: "flex",
    alignItems: "center",
    justifyContent: "flex-end"
  }
};

type Props = {
  repository: Repository,
  changeset: Changeset,
  t: any,
  classes: any
};

class ChangesetRow extends React.Component<Props> {
  createChangesetId = (changeset: Changeset) => {
    const { repository } = this.props;
    return <ChangesetId changeset={changeset} repository={repository} />;
  };

  render() {
    const { repository, changeset, classes } = this.props;
    const description = parseDescription(changeset.description);
    const changesetId = this.createChangesetId(changeset);
    const dateFromNow = <DateFromNow date={changeset.date} />;

    return (
      <div className={classes.changeset}>
        <div className="columns is-gapless is-mobile">
          <div className="column is-three-fifths">
            <div className="columns is-gapless">
              <div className="column is-four-fifths">
                <h4 className="has-text-weight-bold is-ellipsis-overflow">
                  <ExtensionPoint
                    name="changeset.description"
                    props={{ changeset, value: description.title }}
                    renderAll={false}
                  >
                    {description.title}
                  </ExtensionPoint>
                </h4>
                <div className="media">
                  <AvatarWrapper>
                    <figure
                      className={classNames(classes.avatarFigure, "media-left")}
                    >
                      <div className={classNames("image", classes.avatarImage)}>
                        <AvatarImage person={changeset.author} />
                      </div>
                    </figure>
                  </AvatarWrapper>
                  <div className={classNames(classes.metadata, "media-right")}>
                    <p className="is-hidden-touch">
                      <Interpolate
                        i18nKey="changeset.summary"
                        id={changesetId}
                        time={dateFromNow}
                      />
                    </p>
                    <p className="is-hidden-desktop">
                      <Interpolate
                        i18nKey="changeset.shortSummary"
                        id={changesetId}
                        time={dateFromNow}
                      />
                    </p>
                    <p className="is-size-7">
                      <ChangesetAuthor changeset={changeset} />
                    </p>
                  </div>
                </div>
              </div>
              <div className={classNames("column", classes.isVcentered)}>
                <ChangesetTags changeset={changeset} />
              </div>
            </div>
          </div>
          <div className={classNames("column", classes.flexVcenter)}>
            <ChangesetButtonGroup
              repository={repository}
              changeset={changeset}
            />
            <ExtensionPoint
              name="changeset.right"
              props={{ repository, changeset }}
              renderAll={true}
            />
          </div>
        </div>
      </div>
    );
  }
}

export default injectSheet(styles)(translate("repos")(ChangesetRow));
