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
import { Changeset, Repository } from "@scm-manager/ui-types";
import DateFromNow from "../../DateFromNow";
import { AvatarWrapper, AvatarImage } from "../../avatar";
import { parseDescription } from "./changesets";
import ChangesetId from "./ChangesetId";
import ChangesetAuthor from "./ChangesetAuthor";
import ChangesetTags from "./ChangesetTags";
import ChangesetButtonGroup from "./ChangesetButtonGroup";
import ChangesetDescription from "./ChangesetDescription";
import {FileControlFactory} from "../DiffTypes";

type Props = WithTranslation & {
  repository: Repository;
  changeset: Changeset;
};

const Wrapper = styled.div`
  // & references parent rule
  // have a look at https://cssinjs.org/jss-plugin-nested?v=v10.0.0-alpha.9
  & + & {
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid rgba(219, 219, 219, 0.5);
  }
`;

const AvatarFigure = styled.figure`
  margin-top: 0.5rem;
  margin-right: 0.5rem;
`;

const FixedSizedAvatar = styled.div`
  width: 35px;
  height: 35px;
`;

const Metadata = styled.div`
  margin-left: 0;
  width: 100%;
`;

const AuthorWrapper = styled.p`
  margin-top: 0.5rem;
`;

const VCenteredColumn = styled.div`
  align-self: center;
`;

const VCenteredChildColumn = styled.div`
  align-items: center;
  justify-content: flex-end;
`;

class ChangesetRow extends React.Component<Props> {
  createChangesetId = (changeset: Changeset) => {
    const { repository } = this.props;
    return <ChangesetId changeset={changeset} repository={repository} />;
  };

  render() {
    const { repository, changeset } = this.props;
    const description = parseDescription(changeset.description);
    const changesetId = this.createChangesetId(changeset);
    const dateFromNow = <DateFromNow date={changeset.date} />;

    return (
      <Wrapper>
        <div className="columns is-gapless is-mobile">
          <div className="column is-three-fifths">
            <div className="columns is-gapless">
              <div className="column is-four-fifths">
                <div className="media">
                  <AvatarWrapper>
                    <AvatarFigure className="media-left">
                      <FixedSizedAvatar className="image">
                        <AvatarImage person={changeset.author}/>
                      </FixedSizedAvatar>
                    </AvatarFigure>
                  </AvatarWrapper>
                  <Metadata className="media-right">
                    <h4 className="has-text-weight-bold is-ellipsis-overflow">
                      <ExtensionPoint
                        name="changeset.description"
                        props={{
                          changeset,
                          value: description.title
                        }}
                        renderAll={false}
                      >
                        <ChangesetDescription changeset={changeset} value={description.title} />
                      </ExtensionPoint>
                    </h4>
                    <p className="is-hidden-touch">
                      <Trans i18nKey="repos:changeset.summary" components={[changesetId, dateFromNow]}/>
                    </p>
                    <p className="is-hidden-desktop">
                      <Trans i18nKey="repos:changeset.shortSummary" components={[changesetId, dateFromNow]}/>
                    </p>
                    <AuthorWrapper className="is-size-7 is-ellipsis-overflow">
                      <ChangesetAuthor changeset={changeset}/>
                    </AuthorWrapper>
                  </Metadata>
                </div>
              </div>
              <VCenteredColumn className="column">
                <ChangesetTags changeset={changeset}/>
              </VCenteredColumn>
            </div>
          </div>
          <VCenteredChildColumn className={classNames("column", "is-flex")}>
            <ChangesetButtonGroup repository={repository} changeset={changeset}/>
            <ExtensionPoint
              name="changeset.right"
              props={{
                repository,
                changeset
              }}
              renderAll={true}
            />
          </VCenteredChildColumn>
        </div>
      </Wrapper>
    );
  }
}

export default withTranslation("repos")(ChangesetRow);
