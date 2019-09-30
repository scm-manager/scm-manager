//@flow
import React from "react";
import { Interpolate, translate } from "react-i18next";
import injectSheet from "react-jss";
import classNames from "classnames";
import type { Changeset, Repository, Tag } from "@scm-manager/ui-types";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import {
  DateFromNow,
  ChangesetId,
  ChangesetTag,
  ChangesetAuthor,
  ChangesetDiff,
  AvatarWrapper,
  AvatarImage,
  changesets,
  Level,
  Button
} from "@scm-manager/ui-components";

type Props = {
  changeset: Changeset,
  repository: Repository,

  // context props
  t: string => string,
  classes: any
};

type State = {
  collapsed: boolean
};

const styles = {
  spacing: {
    marginRight: "1em"
  },
  tags: {
    "& .tag": {
      marginLeft: ".25rem"
    }
  }
};

class ChangesetDetails extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: false
    };
  }

  render() {
    const { changeset, repository, classes, t } = this.props;
    const { collapsed } = this.state;

    const description = changesets.parseDescription(changeset.description);

    const id = (
      <ChangesetId repository={repository} changeset={changeset} link={false} />
    );
    const date = <DateFromNow date={changeset.date} />;

    return (
      <>
        <div className="content">
          <h4>
            <ExtensionPoint
              name="changeset.description"
              props={{ changeset, value: description.title }}
              renderAll={false}
            >
              {description.title}
            </ExtensionPoint>
          </h4>
          <article className="media">
            <AvatarWrapper>
              <p className={classNames("image", "is-64x64", classes.spacing)}>
                <AvatarImage person={changeset.author} />
              </p>
            </AvatarWrapper>
            <div className="media-content">
              <p>
                <ChangesetAuthor changeset={changeset} />
              </p>
              <p>
                <Interpolate i18nKey="changeset.summary" id={id} time={date} />
              </p>
            </div>
            <div className="media-right">{this.renderTags()}</div>
          </article>
          <p>
            {description.message.split("\n").map((item, key) => {
              return (
                <span key={key}>
                  <ExtensionPoint
                    name="changeset.description"
                    props={{ changeset, value: item }}
                    renderAll={false}
                  >
                    {item}
                  </ExtensionPoint>
                  <br />
                </span>
              );
            })}
          </p>
        </div>
        <div>
          <Level
            right={
              <Button
                action={this.collapseDiffs}
                color="default"
                icon={collapsed ? "eye" : "eye-slash"}
                label={t("changesets.collapseDiffs")}
                reducedMobile={true}
              />
            }
          />
          <ChangesetDiff changeset={changeset} defaultCollapse={collapsed} />
        </div>
      </>
    );
  }

  getTags = () => {
    const { changeset } = this.props;
    return changeset._embedded.tags || [];
  };

  renderTags = () => {
    const { classes } = this.props;
    const tags = this.getTags();
    if (tags.length > 0) {
      return (
        <div className={classNames("level-item", classes.tags)}>
          {tags.map((tag: Tag) => {
            return <ChangesetTag key={tag.name} tag={tag} />;
          })}
        </div>
      );
    }
    return null;
  };

  collapseDiffs = () => {
    this.setState(state => ({
      collapsed: !state.collapsed
    }));
  };
}

export default injectSheet(styles)(translate("repos")(ChangesetDetails));
