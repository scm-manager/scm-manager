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
import { useTranslation } from "react-i18next";
import { createDiffUrl } from "@scm-manager/ui-api";
import { LoadingDiff, Subtitle } from "@scm-manager/ui-components";
import CompareSelectBar, { CompareTypes } from "./CompareSelectBar";
import CompareTabs from "./CompareTabs";
import IncomingChangesets from "./IncomingChangesets";
import { REPOSITORY_SUB_ROUTES, useCurrentRepositoryBaseUrl, useRepositoryFromParams } from "@scm-manager/ui-core";

export type CompareBranchesParams = {
  sourceType: CompareTypes;
  sourceName: string;
  targetType: CompareTypes;
  targetName: string;
};

const CompareRoutes: FC = () => {
  const repository = useRepositoryFromParams();
  const baseUrl = useCurrentRepositoryBaseUrl() + `/${REPOSITORY_SUB_ROUTES.CODE}/${REPOSITORY_SUB_ROUTES.COMPARE}`;
  const { sourceType, sourceName, targetType, targetName } = useParams<CompareBranchesParams>();
  const source = sourceName!;
  const target = targetName!;

  return (
    <Routes>
      <Route
        index
        element={
          <Navigate
            to={`${baseUrl}/${sourceType}/${encodeURIComponent(source)}/${targetType}/${encodeURIComponent(
              target,
            )}/diff/`}
            replace
          />
        }
      />
      <Route
        path="diff/"
        element={<LoadingDiff url={createDiffUrl(repository, source, target) + "?format=GIT"} stickyHeader />}
      />
      <Route
        path="changesets/:page?"
        element={<IncomingChangesets repository={repository} source={source} target={target} />}
      />
    </Routes>
  );
};

const CompareView: FC = () => {
  const [t] = useTranslation("repos");
  const repository = useRepositoryFromParams();
  const baseUrl = useCurrentRepositoryBaseUrl() + `/${REPOSITORY_SUB_ROUTES.CODE}/${REPOSITORY_SUB_ROUTES.COMPARE}`;
  if (!repository._links.incomingDiff) {
    return null;
  }

  return (
    <>
      <Subtitle subtitle={t("compare.title")} />
      <CompareSelectBar repository={repository} baseUrl={baseUrl} />
      <CompareTabs baseUrl={baseUrl} />
      <CompareRoutes />
    </>
  );
};

export default CompareView;
