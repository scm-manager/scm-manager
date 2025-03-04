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
import { SubSubtitle } from "@scm-manager/ui-core";
import { Button, Icon } from "@scm-manager/ui-buttons";
import ContributorTable from "./ContributorTable";
import { Link, Link as ReactLink } from "react-router-dom";
import CreateTagModal from "./CreateTagModal";
import { useContainedInTags } from "@scm-manager/ui-api";
import RevertModal from "./RevertModal";

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

const SeparatedParents = styled.div`
  a + a:before {
    content: ",\\00A0";
    color: #4a4a4a;
  }
`;

const Contributors: FC<{ changeset: Changeset; repository: Repository }> = ({ changeset, repository }) => {
  const [t] = useTranslation("repos");
  const [open, setOpen] = useState(false);
  const [tagCreationModalVisible, setTagCreationModalVisible] = useState(false);
  const signatureIcon =
    changeset?.signatures && changeset.signatures.length > 0 ? (
      <SignatureIcon className="mx-2" signatures={changeset.signatures} />
    ) : (
      <>&nbsp;</>
    );
  const showCreateTagButton = "tag" in changeset._links;
  return (
    <div className="is-flex is-flex-wrap-wrap">
      <details className="mb-2" onClick={() => setOpen(!open)}>
        <summary className="is-flex is-flex-direction-row is-clickable" aria-label={t("changeset.contributors.list")}>
          {open ? (
            <>
              <Icon alt={t("changeset.contributors.hideList")}>angle-down</Icon>
              <span>{t("changeset.contributors.list")}</span> {signatureIcon}
            </>
          ) : (
            <>
              <Icon alt={t("changeset.contributors.showList")}>angle-right</Icon>{" "}
              <span>
                <ChangesetAuthor changeset={changeset} />
              </span>
              <span>{signatureIcon}</span>{" "}
              <span>{t("changeset.contributors.count", { count: countContributors(changeset) })}</span>
            </>
          )}
        </summary>
        <ContributorTable changeset={changeset} />
      </details>
      <div className="is-flex has-gap-2 ml-auto">
        <ChangesetTags changeset={changeset} />
        {showCreateTagButton && (
          <Button className="tag is-success has-gap-1" onClick={() => setTagCreationModalVisible(true)}>
            <Icon>plus</Icon>
            {(changeset._embedded?.tags?.length === 0 && t("changeset.tag.create")) || ""}
          </Button>
        )}
      </div>
      {tagCreationModalVisible && (
        <CreateTagModal
          repository={repository}
          changeset={changeset}
          onClose={() => setTagCreationModalVisible(false)}
        />
      )}
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
  return (
    <details className="mb-2" onClick={() => setOpen(!open)}>
      <summary className="is-flex is-flex-direction-row is-clickable">
        {open ? (
          <>
            <Icon alt={t("changeset.containedInTags.hideAllTags")}>angle-down</Icon>
            {t("changeset.containedInTags.allTags")}
          </>
        ) : (
          <>
            <Icon alt={t("changeset.containedInTags.showAllTags")}>angle-right</Icon>
            {t("changeset.containedInTags.containedInTag", { count: tags.length })}
          </>
        )}
      </summary>
      {tags.map((tag) => (
        <span className="tag is-info is-normal m-1" key={tag.name}>
          <Link to={`/repo/${repository.namespace}/${repository.name}/tag/${tag.name}`} className="has-text-inherit">
            {tag.name}
          </Link>
        </span>
      ))}
    </details>
  );
};

const ChangesetDetails: FC<Props> = ({ changeset, repository, fileControlFactory }) => {
  const [revertModalVisible, setRevertModalVisible] = useState(false);
  const [t] = useTranslation("repos");

  const description = changesets.parseDescription(changeset.description);
  const id = <ChangesetId repository={repository} changeset={changeset} link={false} />;
  const date = <DateFromNow date={changeset.date} />;
  const parents = changeset._embedded?.parents?.map((parent: ParentChangeset, index: number) => (
    <ReactLink title={parent.id} to={parent.id} key={index}>
      {parent.id.substring(0, 7)}
    </ReactLink>
  ));
  const showRevertButton = "revert" in changeset._links;

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
          <div className="media-content pr-2 pt-2 ">
            <Contributors changeset={changeset} repository={repository} />
            <ContainedInTags changeset={changeset} repository={repository} />
            <div className="is-flex is-flex-wrap-wrap">
              <p>
                <Trans i18nKey="repos:changeset.summary" components={[id, date]} />
              </p>
              {parents && parents?.length > 0 ? (
                <SeparatedParents className="ml-4">
                  {t("changeset.parents.label", { count: parents?.length }) + ": "}
                  {parents}
                </SeparatedParents>
              ) : null}
              {showRevertButton && (
                <Button
                  className="tag ml-auto"
                  variant="tertiary"
                  onClick={() => setRevertModalVisible(true)}
                >
                  {t("changeset.revert.button")}
                </Button>
              )}
            </div>
          </div>
          {revertModalVisible && (
            <RevertModal repository={repository} changeset={changeset} onClose={() => setRevertModalVisible(false)} />
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
        <ChangesetDiff changeset={changeset} fileControlFactory={fileControlFactory} />
      </div>
    </>
  );
};

export default ChangesetDetails;
