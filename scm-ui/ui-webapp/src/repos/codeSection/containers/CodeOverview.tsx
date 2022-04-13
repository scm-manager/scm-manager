/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
