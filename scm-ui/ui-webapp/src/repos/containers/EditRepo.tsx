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
import { useTranslation } from "react-i18next";
import { useUpdateRepository } from "@scm-manager/ui-api";
import { ErrorNotification, Subtitle } from "@scm-manager/ui-components";
import { useDocumentTitleForRepository, useRepositoryFromParams } from "@scm-manager/ui-core";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import UpdateNotification from "../../components/UpdateNotification";
import RepositoryForm from "../components/form";
import Reindex from "../components/Reindex";
import RepositoryDangerZone from "./RepositoryDangerZone";
import ExportRepository from "./ExportRepository";
import HealthCheckWarning from "./HealthCheckWarning";
import RunHealthCheck from "./RunHealthCheck";

const EditRepo: FC = () => {
  const repository = useRepositoryFromParams();
  const { isPending, error, update, isUpdated } = useUpdateRepository();
  const [t] = useTranslation("repos");
  useDocumentTitleForRepository(repository, t("repositoryRoot.settingsTitle"));

  const extensionProps = {
    repository,
  };

  return (
    <>
      <HealthCheckWarning repository={repository} />
      <Subtitle subtitle={t("repositoryForm.subtitle")} />
      <UpdateNotification isUpdated={isUpdated} />
      <ErrorNotification error={error} />
      <RepositoryForm repository={repository} loading={isPending} modifyRepository={update} />
      <ExtensionPoint<extensionPoints.RepoConfigDetails>
        name="repo-config.details"
        props={extensionProps}
        renderAll={true}
      />
      {repository._links.exportInfo && <ExportRepository repository={repository} />}
      {(repository._links.runHealthCheck || repository.healthCheckRunning) && (
        <RunHealthCheck repository={repository} />
      )}
      {repository._links.reindex ? <Reindex repository={repository} /> : null}
      <RepositoryDangerZone repository={repository} />
    </>
  );
};

export default EditRepo;
