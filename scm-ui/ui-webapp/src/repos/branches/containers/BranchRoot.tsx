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
import BranchView from "../components/BranchView";
import { Redirect, Route, Switch, useLocation, useRouteMatch } from "react-router-dom";
import { Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, NotFoundError, urls } from "@scm-manager/ui-components";
import queryString from "query-string";
import { useBranch } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
};

type Params = {
  branch: string;
};

const BranchRoot: FC<Props> = ({ repository }) => {
  const match = useRouteMatch<Params>();
  const { isLoading, error, data: branch } = useBranch(repository, decodeURIComponent(match.params.branch));
  const location = useLocation();

  if (isLoading) {
    return <Loading />;
  }

  if (error) {
    if (error instanceof NotFoundError && queryString.parse(location.search).create === "true") {
      return (
        <Redirect to={`/repo/${repository.namespace}/${repository.name}/branches/create?name=${match.params.branch}`} />
      );
    }

    return <ErrorNotification error={error} />;
  }

  const url = urls.matchedUrlFromMatch(match);
  if (!branch) {
    return null;
  }

  const escapedUrl = urls.escapeUrlForRoute(url);

  return (
    <Switch>
      <Redirect exact from={escapedUrl} to={`${url}/info`} />
      <Route path={`${escapedUrl}/info`}>
        <BranchView repository={repository} branch={branch} />
      </Route>
    </Switch>
  );
};

export default BranchRoot;
