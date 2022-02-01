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
import { Redirect, Route, Switch, useRouteMatch } from "react-router-dom";
import { Repository } from "@scm-manager/ui-types";
import { useBranches } from "@scm-manager/ui-api";
import { ErrorNotification, Loading, urls } from "@scm-manager/ui-components";
import CompareView, { CompareBranchesParams } from "./CompareView";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const CompareRoot: FC<Props> = ({ repository, baseUrl }) => {
  const match = useRouteMatch<CompareBranchesParams>();
  const { data, isLoading, error } = useBranches(repository);
  const url = urls.matchedUrlFromMatch(match);

  if (isLoading || !data) {
    return <Loading />;
  }
  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <Switch>
      <Route path={`${baseUrl}/:sourceType/:sourceName/:targetType/:targetName`}>
        <CompareView repository={repository} baseUrl={baseUrl} />
      </Route>
      {data._embedded && (
        <Redirect from={url} to={`${url}/b/${data._embedded.branches.filter(b => b.defaultBranch)[0].name}`} />
      )}
    </Switch>
  );
};

export default CompareRoot;
