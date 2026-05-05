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
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import { REPOSITORY_SUB_ROUTES, useDocumentTitleForRepository } from "@scm-manager/ui-core";
import { useBranches } from "@scm-manager/ui-api";
import BranchTableWrapper from "./BranchTableWrapper";
import { useCurrentRepositoryBaseUrl, useRepositoryFromParams } from "@scm-manager/ui-core";

const BranchesOverview: FC = () => {
  const repository = useRepositoryFromParams();
  const baseUrl = useCurrentRepositoryBaseUrl();
  const { isLoading, error, data } = useBranches(repository);
  const [t] = useTranslation("repos");
  useDocumentTitleForRepository(repository, t("branches.overview.title"));

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!data || isLoading) {
    return <Loading />;
  }

  return (
    <BranchTableWrapper repository={repository} baseUrl={`${baseUrl}/${REPOSITORY_SUB_ROUTES.BRANCH}`} data={data} />
  );
};

export default BranchesOverview;
