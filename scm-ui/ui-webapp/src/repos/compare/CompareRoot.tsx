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
import { Navigate, Route, Routes, useParams } from "react-router-dom";
import { Branch } from "@scm-manager/ui-types";
import { useBranches } from "@scm-manager/ui-api";
import {
  Loading,
  REPOSITORY_SUB_ROUTES,
  useCurrentRepositoryBaseUrl,
  useRepositoryFromParams,
} from "@scm-manager/ui-core";
import CompareView, { CompareBranchesParams } from "./CompareView";

const DefaultCompareRedirect: FC<{ branches?: Branch[]; baseUrl: string }> = ({ branches, baseUrl }) => {
  const { sourceType, sourceName } = useParams<CompareBranchesParams>();
  const defaultBranch = branches?.find((b) => b.defaultBranch);
  if (!defaultBranch) {
    return <Loading />;
  }
  const destination = `${baseUrl}/${sourceType}/${encodeURIComponent(sourceName!)}/b/${encodeURIComponent(
    defaultBranch.name,
  )}`;
  return <Navigate to={destination} replace />;
};

const CompareRoot: FC = () => {
  const repository = useRepositoryFromParams();
  const baseUrl = useCurrentRepositoryBaseUrl() + `/${REPOSITORY_SUB_ROUTES.CODE}`;
  const { data } = useBranches(repository);

  return (
    <Routes>
      <Route
        index
        element={
          <DefaultCompareRedirect
            branches={data?._embedded?.branches ?? []}
            baseUrl={`${baseUrl}/${REPOSITORY_SUB_ROUTES.COMPARE}`}
          />
        }
      />
      <Route path=":targetType/:targetName/*" element={<CompareView />} />
    </Routes>
  );
};

export default CompareRoot;
