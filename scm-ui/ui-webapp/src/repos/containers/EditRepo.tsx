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
import { useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { useUpdateRepository } from "@scm-manager/ui-api";
import { ErrorNotification, Subtitle, urls } from "@scm-manager/ui-components";
import { useDocumentTitleForRepository } from "@scm-manager/ui-core";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import UpdateNotification from "../../components/UpdateNotification";
import RepositoryForm from "../components/form";
import Reindex from "../components/Reindex";
import RepositoryDangerZone from "./RepositoryDangerZone";
import ExportRepository from "./ExportRepository";
import HealthCheckWarning from "./HealthCheckWarning";
import RunHealthCheck from "./RunHealthCheck";

type Props = {
  repository: Repository;
};

const EditRepo: FC<Props> = ({ repository }) => {
  const match = useRouteMatch();
  const { isLoading, error, update, isUpdated } = useUpdateRepository();
  const [t] = useTranslation("repos");
  useDocumentTitleForRepository(repository, t("repositoryRoot.settingsTitle"));

  const url = urls.matchedUrlFromMatch(match);
  const extensionProps = {
    repository,
    url,
  };

  return (
    <>
      <HealthCheckWarning repository={repository} />
      <Subtitle subtitle={t("repositoryForm.subtitle")} />
      <UpdateNotification isUpdated={isUpdated} />
      <ErrorNotification error={error} />
      <RepositoryForm repository={repository} loading={isLoading} modifyRepository={update} />
      <ExtensionPoint<extensionPoints.RepoConfigDetails>
        name="repo-config.details"
        props={extensionProps}
        renderAll={true}
      />
      {repository._links.exportInfo && <ExportRepository repository={repository} />}
      <ExtensionPoint<extensionPoints.RepoConfigRoute>
        name="repo-config.route"
        props={extensionProps}
        renderAll={true}
      />
      {(repository._links.runHealthCheck || repository.healthCheckRunning) && (
        <RunHealthCheck repository={repository} />
      )}
      {repository._links.reindex ? <Reindex repository={repository} /> : null}
      <RepositoryDangerZone repository={repository} />
    </>
  );
};

export default EditRepo;
