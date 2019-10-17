//@flow
import React from "react";
import { Interpolate, translate } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Changeset, Repository } from "@scm-manager/ui-types";
import DateFromNow from "../../DateFromNow";
import { AvatarWrapper, AvatarImage } from "../../avatar";
import { parseDescription } from "./changesets";
import ChangesetId from "./ChangesetId";
import ChangesetAuthor from "./ChangesetAuthor";
import ChangesetTags from "./ChangesetTags";
import ChangesetButtonGroup from "./ChangesetButtonGroup";

type Props = {
  repository: Repository,
  changeset: Changeset,

  // context props
  t: string => string
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
                        <AvatarImage person={changeset.author} />
                      </FixedSizedAvatar>
                    </AvatarFigure>
                  </AvatarWrapper>
                  <Metadata className="media-right">
                    <h4 className="has-text-weight-bold is-ellipsis-overflow">
                      <ExtensionPoint
                        name="changeset.description"
                        props={{ changeset, value: description.title }}
                        renderAll={false}
                      >
                        {description.title}
                      </ExtensionPoint>
                    </h4>
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
                    <AuthorWrapper className="is-size-7">
                      <ChangesetAuthor changeset={changeset} />
                    </AuthorWrapper>
                  </Metadata>
                </div>
              </div>
              <VCenteredColumn className="column">
                <ChangesetTags changeset={changeset} />
              </VCenteredColumn>
            </div>
          </div>
          <VCenteredChildColumn className={classNames("column", "is-flex")}>
            <ChangesetButtonGroup
              repository={repository}
              changeset={changeset}
            />
            <ExtensionPoint
              name="changeset.right"
              props={{ repository, changeset }}
              renderAll={true}
            />
          </VCenteredChildColumn>
        </div>
      </Wrapper>
    );
  }
}

export default translate("repos")(ChangesetRow);
