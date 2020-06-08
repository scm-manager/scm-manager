/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { Trans, WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Changeset, Repository, Tag } from "@scm-manager/ui-types";
import {
  AvatarImage,
  AvatarWrapper,
  Button,
  ChangesetDiff,
  ChangesetId,
  changesets,
  ChangesetTag,
  DateFromNow,
  Level
} from "@scm-manager/ui-components";
import ContributorTable from "./ContributorTable";

type Props = WithTranslation & {
  changeset: Changeset;
  repository: Repository;
};

type State = {
  collapsed: boolean;
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
    const id = <ChangesetId repository={repository} changeset={changeset} link={false} />;
    const date = <DateFromNow date={changeset.date} />;

    return (
      <>
        <div className={classNames("content", "is-marginless")}>
          <h4>
            <ExtensionPoint
              name="changeset.description"
              props={{
                changeset,
                value: description.title
              }}
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
              <ContributorTable changeset={changeset} />
              <p>
                <Trans i18nKey="repos:changeset.summary" components={[id, date]} />
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
                    props={{
                      changeset,
                      value: item
                    }}
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

export default withTranslation("repos")(ChangesetDetails);
