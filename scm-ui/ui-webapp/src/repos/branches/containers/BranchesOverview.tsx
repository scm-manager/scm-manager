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
import { Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import { useDocumentTitleForRepository } from "@scm-manager/ui-core";
import { useBranches } from "@scm-manager/ui-api";
import BranchTableWrapper from "./BranchTableWrapper";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const BranchesOverview: FC<Props> = ({ repository, baseUrl }) => {
  const { isLoading, error, data } = useBranches(repository);
  const [t] = useTranslation("repos");
  useDocumentTitleForRepository(repository, t("branches.overview.title"));

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!data || isLoading) {
    return <Loading />;
  }

  return <BranchTableWrapper repository={repository} baseUrl={baseUrl} data={data} />;
};

export default BranchesOverview;
