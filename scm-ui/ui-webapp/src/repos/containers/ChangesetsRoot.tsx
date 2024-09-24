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
import { Route, useRouteMatch, useHistory, useLocation } from "react-router-dom";
import { Repository, Branch } from "@scm-manager/ui-types";
import CodeActionBar from "../codeSection/components/CodeActionBar";
import { urls } from "@scm-manager/ui-components";
import Changesets from "./Changesets";
import { RepositoryRevisionContextProvider } from "@scm-manager/ui-api";
import { encodeFilePath, encodePart } from "../sources/components/content/FileLink";

type Props = {
  repository: Repository;
  baseUrl: string;
  branches?: Branch[];
  selectedBranch?: string;
};

const ChangesetRoot: FC<Props> = ({ repository, baseUrl, branches, selectedBranch }) => {
  const match = useRouteMatch();
  const history = useHistory();
  const location = useLocation();
  if (!repository) {
    return null;
  }

  const url = urls.stripEndingSlash(urls.escapeUrlForRoute(match.url));
  const defaultBranch = branches?.find((b) => b.defaultBranch === true);

  const isBranchAvailable = () => {
    return branches?.filter((b) => b.name === selectedBranch).length === 0;
  };

  const evaluateSwitchViewLink = () => {
    const sourcePath = encodeFilePath(urls.getPrevSourcePathFromLocation(location) || "");

    if (selectedBranch) {
      return `${baseUrl}/sources/${encodePart(selectedBranch)}/${sourcePath}`;
    }

    if (repository.type === "svn") {
      return `${baseUrl}/sources/${sourcePath !== "/" ? sourcePath : ""}`;
    }

    return `${baseUrl}/sources/`;
  };

  const onSelectBranch = (branch?: Branch) => {
    if (branch) {
      history.push(`${baseUrl}/branch/${encodePart(branch.name)}/changesets/${location.search}`);
    } else {
      history.push(`${baseUrl}/changesets/${location.search}`);
    }
  };

  return (
    <RepositoryRevisionContextProvider revision={selectedBranch}>
      <CodeActionBar
        branches={branches}
        selectedBranch={!isBranchAvailable() ? selectedBranch : defaultBranch?.name}
        onSelectBranch={onSelectBranch}
        switchViewLink={evaluateSwitchViewLink()}
      />
      <Route path={`${url}/:page?`}>
        <Changesets repository={repository} branch={branches?.filter((b) => b.name === selectedBranch)[0]} url={url} />
      </Route>
    </RepositoryRevisionContextProvider>
  );
};

export default ChangesetRoot;
