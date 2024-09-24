/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC } from "react";
import classNames from "classnames";
import { AvatarImage, AvatarWrapper } from "../../avatar";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import ChangesetDescription from "./ChangesetDescription";
import { Trans } from "react-i18next";
import ChangesetAuthor from "./ChangesetAuthor";
import SignatureIcon from "./SignatureIcon";
import ChangesetTags from "./ChangesetTags";
import { parseDescription } from "./changesets";
import DateFromNow from "../../DateFromNow";
import { Changeset, Repository } from "@scm-manager/ui-types";
import styled from "styled-components";
import ChangesetId from "./ChangesetId";

type Props = {
  repository: Repository;
  changeset: Changeset;
};

const FixedSizedAvatar = styled.div`
  width: 35px;
  height: 35px;
`;

const FullWidthDiv = styled.div`
  width: 100%;
`;

const SingleChangeset: FC<Props> = ({ repository, changeset }) => {
  const createChangesetId = () => {
    return <ChangesetId changeset={changeset} repository={repository} />;
  };

  const description = parseDescription(changeset.description);
  const changesetId = createChangesetId();
  const dateFromNow = <DateFromNow date={changeset.date} />;

  return (
    <div className={classNames("columns", "is-gapless")}>
      <div className={classNames("column", "is-four-fifths")}>
        <div className="media">
          <AvatarWrapper>
            <figure className={classNames("media-left", "mt-2", "mr-2")}>
              <FixedSizedAvatar className="image">
                <AvatarImage person={changeset.author} />
              </FixedSizedAvatar>
            </figure>
          </AvatarWrapper>
          <FullWidthDiv className={classNames("media-right", "ml-0")}>
            <h4 className={classNames("has-text-weight-bold", "is-ellipsis-overflow")}>
              <ExtensionPoint<extensionPoints.ChangesetDescription>
                name="changeset.description"
                props={{
                  changeset,
                  value: description.title,
                }}
                renderAll={false}
              >
                <ChangesetDescription changeset={changeset} value={description.title} />
              </ExtensionPoint>
            </h4>
            <p className="is-hidden-touch">
              <Trans i18nKey="repos:changeset.summary" components={[changesetId, dateFromNow]} />
            </p>
            <p className="is-hidden-desktop">
              <Trans i18nKey="repos:changeset.shortSummary" components={[changesetId, dateFromNow]} />
            </p>
            <div className="is-flex">
              <p className={classNames("is-size-7", "is-ellipsis-overflow", "mt-2")}>
                <ChangesetAuthor changeset={changeset} />
              </p>
              {changeset?.signatures && changeset.signatures.length > 0 && (
                <SignatureIcon className={classNames("mx-2", "pt-1")} signatures={changeset.signatures} />
              )}
            </div>
          </FullWidthDiv>
        </div>
      </div>
      <div className={classNames("column", "is-align-self-center")}>
        <ChangesetTags changeset={changeset} />
      </div>
    </div>
  );
};

export default SingleChangeset;
