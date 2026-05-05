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
import { useNavigate, useLocation, Outlet } from "react-router-dom";
import CodeActionBar from "../codeSection/components/CodeActionBar";
import { urls } from "@scm-manager/ui-components";
import { RepositoryRevisionContextProvider, useBranchesIfAvailable } from "@scm-manager/ui-api";
import { encodeFilePath } from "../sources/components/content/FileLink";
import {
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  REPOSITORY_CODE_SUB_ROUTES,
  useSourceNameFromParams,
} from "@scm-manager/ui-core";
import { CompareTypes } from "../compare/CompareSelectBar";

const ChangesetRoot: FC = () => {
  const repository = useRepositoryFromParams();
  const baseUrl = useCurrentRepositoryBaseUrl() + "/code";
  const { data } = useBranchesIfAvailable(repository);
  const sourceName = useSourceNameFromParams();
  const navigate = useNavigate();
  const location = useLocation();
  if (!repository) {
    return null;
  }

  const hasContent = data?._embedded?.branches && data._embedded.branches.length > 0;

  const evaluateSwitchViewLink = () => {
    const sourcePath = encodeFilePath(urls.getPrevSourcePathFromLocation(location) || "");

    if (sourceName) {
      return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${encodeURIComponent(sourceName)}/${sourcePath}`;
    }

    if (repository.type === "svn") {
      return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${sourcePath !== "/" ? sourcePath : ""}`;
    }

    return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/`;
  };

  const onSelect = (type: CompareTypes, name: string) => {
    if (name) {
      navigate(
        `${baseUrl}/${type}/${encodeURIComponent(name)}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/${location.search}`,
      );
    } else {
      navigate(`${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/${location.search}`);
    }
  };

  return (
    <RepositoryRevisionContextProvider revision={sourceName}>
      <CodeActionBar hasContent={hasContent} onSelect={onSelect} switchViewLink={evaluateSwitchViewLink()} />
      <Outlet />
    </RepositoryRevisionContextProvider>
  );
};

export default ChangesetRoot;
