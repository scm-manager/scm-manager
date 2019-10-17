//@flow
import React from "react";
import { Interpolate, translate } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Changeset, Repository, Tag } from "@scm-manager/ui-types";
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
  t: string => string
};

type State = {
  collapsed: boolean
};

const RightMarginP = styled.p`
  margin-right: 1em;
`;

const TagsWrapper = styled.div`
  & .tag {
    margin-left: 0.25rem;
  }
`;

const BottomMarginLevel = styled(Level)`
  margin-bottom: 1rem !important;
`;

class ChangesetDetails extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: false
    };
  }

  render() {
    const { changeset, repository, t } = this.props;
    const { collapsed } = this.state;

    const description = changesets.parseDescription(changeset.description);
    const id = (
      <ChangesetId repository={repository} changeset={changeset} link={false} />
    );
    const date = <DateFromNow date={changeset.date} />;

    return (
      <>
        <div className={classNames("content", "is-marginless")}>
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
              <RightMarginP className={classNames("image", "is-64x64")}>
                <AvatarImage person={changeset.author} />
              </RightMarginP>
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
          <BottomMarginLevel
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
    return this.props.changeset._embedded.tags || [];
  };

  renderTags = () => {
    const tags = this.getTags();
    if (tags.length > 0) {
      return (
        <TagsWrapper className="level-item">
          {tags.map((tag: Tag) => {
            return <ChangesetTag key={tag.name} tag={tag} />;
          })}
        </TagsWrapper>
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

export default translate("repos")(ChangesetDetails);
