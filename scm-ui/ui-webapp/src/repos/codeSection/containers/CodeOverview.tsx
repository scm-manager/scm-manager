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
import { Route, useLocation } from "react-router-dom";
import Sources from "../../sources/containers/Sources";
import ChangesetsRoot from "../../containers/ChangesetsRoot";
import { Branch, Repository } from "@scm-manager/ui-types";
import { ErrorPage, Loading, urls } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useBranches } from "@scm-manager/ui-api";
import FileSearch from "./FileSearch";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const useSelectedBranch = () => {
  const location = useLocation();
  const branchFromURL =
    !location.pathname.includes("/code/changesets/") && decodeURIComponent(location.pathname.split("/")[6]);
  return branchFromURL && branchFromURL !== "undefined" ? branchFromURL : "";
};

const supportBranches = (repository: Repository) => {
  return !!repository._links.branches;
};

const CodeOverview: FC<Props> = ({ baseUrl, repository }) => {
  if (supportBranches(repository)) {
    return <CodeOverviewWithBranches baseUrl={baseUrl} repository={repository} />;
  }
  return <CodeRouting baseUrl={baseUrl} repository={repository} />;
};

const CodeOverviewWithBranches: FC<Props> = ({ repository, baseUrl }) => {
  const { isLoading, error, data } = useBranches(repository);
  const selectedBranch = useSelectedBranch();
  const [t] = useTranslation("repos");
  const branches = data?._embedded.branches || [];

  if (isLoading) {
    return <Loading />;
  }

  if (error) {
    return (
      <ErrorPage title={t("repositoryRoot.errorTitle")} subtitle={t("repositoryRoot.errorSubtitle")} error={error} />
    );
  }

  if (branches.length === 0) {
    return <Sources repository={repository} baseUrl={baseUrl} />;
  }

  return <CodeRouting repository={repository} baseUrl={baseUrl} branches={branches} selectedBranch={selectedBranch} />;
};

type RoutingProps = {
  repository: Repository;
  baseUrl: string;
  branches?: Branch[];
  selectedBranch?: string;
};

const CodeRouting: FC<RoutingProps> = ({ repository, baseUrl, branches, selectedBranch }) => {

  const escapedUrl = urls.escapeUrlForRoute(baseUrl);
  return (
    <>
      <Route path={`${escapedUrl}/sources`} exact={true}>
        <Sources repository={repository} baseUrl={baseUrl} branches={branches} />
      </Route>
      <Route path={`${escapedUrl}/sources/:revision/:path*`}>
        <Sources repository={repository} baseUrl={baseUrl} branches={branches} selectedBranch={selectedBranch} />
      </Route>
      <Route path={`${escapedUrl}/changesets`}>
        <ChangesetsRoot repository={repository} baseUrl={baseUrl} branches={branches} />
      </Route>
      <Route path={`${escapedUrl}/branch/:branch/changesets/`}>
        <ChangesetsRoot repository={repository} baseUrl={baseUrl} branches={branches} selectedBranch={selectedBranch} />
      </Route>
      <Route path={`${escapedUrl}/search/:revision/`}>
        <FileSearch repository={repository} baseUrl={baseUrl} branches={branches} selectedBranch={selectedBranch} />
      </Route>
    </>
  );
};

export default CodeOverview;
