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
import React, { FC, useState } from "react";
import { Trans, useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { Changeset, ParentChangeset, Repository } from "@scm-manager/ui-types";
import {
  AvatarImage,
  AvatarWrapper,
  ChangesetAuthor,
  ChangesetDescription,
  ChangesetDiff,
  ChangesetId,
  changesets,
  ChangesetTags,
  DateFromNow,
  FileControlFactory,
  SignatureIcon,
} from "@scm-manager/ui-components";
import { Tooltip, SubSubtitle } from "@scm-manager/ui-core";
import { Button, Icon } from "@scm-manager/ui-buttons";
import ContributorTable from "./ContributorTable";
import { Link, Link as ReactLink } from "react-router-dom";
import CreateTagModal from "./CreateTagModal";
import { useContainedInTags } from "@scm-manager/ui-api";

type Props = {
  changeset: Changeset;
  repository: Repository;
  fileControlFactory?: FileControlFactory;
};

const countContributors = (changeset: Changeset) => {
  if (changeset.contributors) {
    const uniqueContributors: string[] = [];
    changeset.contributors
      .map((p) => p.person)
      .forEach((c) => {
        if (c.mail) {
          if (!uniqueContributors.includes(c.mail)) {
            uniqueContributors.push(c.mail);
          }
        } else {
          uniqueContributors.push(c.name);
        }
      });
    return uniqueContributors.length + 1;
  }
  return 1;
};

const ContributorColumn = styled.div`
  flex-grow: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
`;

const CountColumn = styled.div`
  text-align: right;
  white-space: nowrap;
`;

const SeparatedParents = styled.div`
  a + a:before {
    content: ",\\00A0";
    color: #4a4a4a;
  }
`;

const Contributors: FC<{ changeset: Changeset }> = ({ changeset }) => {
  const [t] = useTranslation("repos");
  const [open, setOpen] = useState(false);
  const signatureIcon =
    changeset?.signatures && changeset.signatures.length > 0 ? (
      <SignatureIcon className="mx-2" signatures={changeset.signatures} />
    ) : (
      <>&nbsp;</>
    );

  if (open) {
    return (
      <div className="is-flex is-flex-direction-column mb-4">
        <div className="is-flex">
          <p className="is-ellipsis-overflow is-clickable mb-2" onClick={() => setOpen(!open)}>
            <Icon alt={t("changeset.contributors.hideList")}>angle-down</Icon> {t("changeset.contributors.list")}
          </p>
          {signatureIcon}
        </div>
        <ContributorTable changeset={changeset} />
      </div>
    );
  }

  return (
    <div className="is-flex is-clickable" onClick={() => setOpen(!open)}>
      <ContributorColumn className="is-ellipsis-overflow">
        <Icon alt={t("changeset.contributors.showList")}>angle-right</Icon> <ChangesetAuthor changeset={changeset} />
      </ContributorColumn>
      {signatureIcon}
      <CountColumn className="is-hidden-mobile is-hidden-tablet-only is-hidden-desktop-only">
        (<span>{t("changeset.contributors.count", { count: countContributors(changeset) })}</span>)
      </CountColumn>
    </div>
  );
};

const ContainedInTags: FC<{ changeset: Changeset; repository: Repository }> = ({ changeset, repository }) => {
  const [t] = useTranslation("repos");
  const [open, setOpen] = useState(false);
  const { data, isLoading } = useContainedInTags(changeset, repository);

  const tags = data?._embedded?.tags;

  if (!tags || tags.length === 0 || isLoading) {
    return <div className="mb-5"></div>;
  }

  if (open) {
    return (
      <div className="is-flex is-flex-direction-column mb-4">
        <div className="is-flex">
          <p className="is-ellipsis-overflow is-clickable mb-2" onClick={() => setOpen(!open)}>
            <Icon alt={t("changeset.containedInTags.hideAllTags")}>angle-down</Icon>{" "}
            {t("changeset.containedInTags.allTags")}
          </p>
        </div>
        <div>
          {" "}
          {tags.map((tag) => (
            <span className="tag is-info is-normal m-1" key={tag.name}>
              <Link
                to={`/repo/${repository.namespace}/${repository.name}/tag/${tag.name}`}
                className="has-text-inherit"
              >
                {tag.name}
              </Link>
            </span>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="is-flex is-clickable" onClick={() => setOpen(!open)}>
      <ContributorColumn className="is-ellipsis-overflow">
        <Icon alt={t("changeset.containedInTags.showAllTags")}>angle-right</Icon>{" "}
        {t("changeset.containedInTags.containedInTag", { count: tags.length })}
      </ContributorColumn>
    </div>
  );
};

const ChangesetDetails: FC<Props> = ({ changeset, repository, fileControlFactory }) => {
  const [isTagCreationModalVisible, setTagCreationModalVisible] = useState(false);
  const [t] = useTranslation("repos");

  const description = changesets.parseDescription(changeset.description);
  const id = <ChangesetId repository={repository} changeset={changeset} link={false} />;
  const date = <DateFromNow date={changeset.date} />;
  const parents = changeset._embedded?.parents?.map((parent: ParentChangeset, index: number) => (
    <ReactLink title={parent.id} to={parent.id} key={index}>
      {parent.id.substring(0, 7)}
    </ReactLink>
  ));
  const showCreateButton = "tag" in changeset._links;

  return (
    <>
      <div className={classNames("content", "m-0")}>
        <SubSubtitle>
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
        </SubSubtitle>
        <article className="media">
          <AvatarWrapper>
            <p className={classNames("image", "is-64x64", "mr-4")}>
              <AvatarImage person={changeset.author} />
            </p>
          </AvatarWrapper>
          <div className="media-content">
            <Contributors changeset={changeset} />
            <ContainedInTags changeset={changeset} repository={repository} />
            <div className="is-flex is-ellipsis-overflow">
              <p>
                <Trans i18nKey="repos:changeset.summary" components={[id, date]} />
              </p>
              {parents && parents?.length > 0 ? (
                <SeparatedParents className="ml-4">
                  {t("changeset.parents.label", { count: parents?.length }) + ": "}
                  {parents}
                </SeparatedParents>
              ) : null}
            </div>
          </div>
          <div className="media-right">
            <ChangesetTags changeset={changeset} />
          </div>

          {showCreateButton && (
            <div className="media-right">
              <Tooltip message={t("changeset.tag.create")}>
                <Button className="tag is-success has-gap-1" onClick={() => setTagCreationModalVisible(true)}>
                  <Icon>plus</Icon>
                  {(changeset._embedded?.tags?.length === 0 && t("changeset.tag.create")) || ""}
                </Button>
              </Tooltip>
            </div>
          )}
          {isTagCreationModalVisible && (
            <CreateTagModal
              repository={repository}
              changeset={changeset}
              onClose={() => setTagCreationModalVisible(false)}
            />
          )}
        </article>
        <p>
          {description.message.split("\n").map((item, key) => {
            return (
              <span key={key}>
                <ExtensionPoint<extensionPoints.ChangesetDescription>
                  name="changeset.description"
                  props={{
                    changeset,
                    value: item,
                  }}
                  renderAll={false}
                >
                  <ChangesetDescription changeset={changeset} value={item} />
                </ExtensionPoint>
                <br />
              </span>
            );
          })}
        </p>
      </div>
      <div>
        <ChangesetDiff
          changeset={changeset}
          fileControlFactory={fileControlFactory}
        />
      </div>
    </>
  );
};

export default ChangesetDetails;
