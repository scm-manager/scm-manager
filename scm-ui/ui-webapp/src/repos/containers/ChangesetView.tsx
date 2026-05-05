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
import { useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Changeset } from "@scm-manager/ui-types";
import { ErrorPage, JumpToFileButton, Loading } from "@scm-manager/ui-components";
import ChangesetDetails from "../components/changesets/ChangesetDetails";
import { FileControlFactory } from "@scm-manager/ui-components";
import { RepositoryRevisionContextProvider, useChangeset } from "@scm-manager/ui-api";
import {
  useDocumentTitle,
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  REPOSITORY_SUB_ROUTES,
  REPOSITORY_CODE_SUB_ROUTES,
} from "@scm-manager/ui-core";

type Params = {
  id: string;
};

const ChangesetView: FC = () => {
  const repository = useRepositoryFromParams();
  const absoluteCodeUrl = useCurrentRepositoryBaseUrl() + "/" + REPOSITORY_SUB_ROUTES.CODE;
  const { id } = useParams<Params>();
  const { isLoading, error, data: changeset } = useChangeset(repository, id);
  const [t] = useTranslation("repos");
  useDocumentTitle(
    changeset?.id
      ? t("changesets.idWithNamespaceName", {
          id: changeset.id.slice(0, 7),
          namespace: repository.namespace,
          name: repository.name,
        })
      : t("changesets.changesetsWithNamespaceName", {
          namespace: repository.namespace,
          name: repository.name,
        }),
  );

  if (error) {
    return <ErrorPage title={t("changesets.errorTitle")} subtitle={t("changesets.errorSubtitle")} error={error} />;
  }

  if (!changeset || isLoading) {
    return <Loading />;
  }

  const fileControlFactoryFactory: (changeset: Changeset) => FileControlFactory = (changeset) => (file) => {
    const sourceLink = file.newPath && {
      url: `${absoluteCodeUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${changeset.id}/${file.newPath}/`,
      label: t("diff.jumpToSource"),
    };
    const targetLink = file.oldPath &&
      changeset._embedded?.parents?.length === 1 && {
        url: `${absoluteCodeUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${changeset._embedded.parents[0].id}/${file.oldPath}`,
        label: t("diff.jumpToTarget"),
      };

    const links = [];
    switch (file.type) {
      case "add":
        if (sourceLink) {
          links.push(sourceLink);
        }
        break;
      case "delete":
        if (targetLink) {
          links.push(targetLink);
        }
        break;
      default:
        if (targetLink && sourceLink) {
          links.push(targetLink, sourceLink); // Target link first because it's the previous file
        } else if (sourceLink) {
          links.push(sourceLink);
        }
    }

    return links ? links.map(({ url, label }) => <JumpToFileButton key={url} tooltip={label} link={url} />) : null;
  };

  return (
    <RepositoryRevisionContextProvider revision={changeset.id}>
      <ChangesetDetails
        changeset={changeset}
        repository={repository}
        fileControlFactory={fileControlFactoryFactory && fileControlFactoryFactory(changeset)}
      />
    </RepositoryRevisionContextProvider>
  );
};

export default ChangesetView;
