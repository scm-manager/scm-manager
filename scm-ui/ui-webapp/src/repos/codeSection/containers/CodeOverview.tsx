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
import { Repository } from "@scm-manager/ui-types";
import { ErrorPage, Loading } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useBranches } from "@scm-manager/ui-api";

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

const CodeOverview: FC<Props> = ({ baseUrl, repository }) => {
  const { isLoading, error, data } = useBranches(repository);
  const selectedBranch = useSelectedBranch();
  const [t] = useTranslation("repos");
  const branches = data?._embedded.branches;

  if (isLoading) {
    return <Loading />;
  }

  if (error) {
    return (
      <ErrorPage title={t("repositoryRoot.errorTitle")} subtitle={t("repositoryRoot.errorSubtitle")} error={error} />
    );
  }


  return (
    <>
      <Route
        path={`${baseUrl}/sources`}
        exact={true}
        render={() => <Sources repository={repository} baseUrl={baseUrl} branches={branches} />}
      />
      <Route
        path={`${baseUrl}/sources/:revision/:path*`}
        render={() => (
          <Sources repository={repository} baseUrl={baseUrl} branches={branches} selectedBranch={selectedBranch} />
        )}
      />
      <Route path={`${baseUrl}/changesets`}>
        <ChangesetsRoot repository={repository} baseUrl={baseUrl} branches={branches} />
      </Route>
      <Route path={`${baseUrl}/branch/:branch/changesets/`}>
        <ChangesetsRoot repository={repository} baseUrl={baseUrl} branches={branches} selectedBranch={selectedBranch} />
      </Route>
    </>
  );
};

export default CodeOverview;
