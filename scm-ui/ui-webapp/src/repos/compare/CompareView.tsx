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
import { useTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { createDiffUrl } from "@scm-manager/ui-api";
import { LoadingDiff, Subtitle, urls } from "@scm-manager/ui-components";
import CompareSelectBar, { CompareTypes } from "./CompareSelectBar";
import CompareTabs from "./CompareTabs";
import IncomingChangesets from "./IncomingChangesets";

type Props = {
  repository: Repository;
  baseUrl: string;
};

export type CompareBranchesParams = {
  sourceType: CompareTypes;
  sourceName: string;
  targetType: CompareTypes;
  targetName: string;
};

const CompareRoutes: FC<Props> = ({ repository, baseUrl }) => {
  const match = useRouteMatch<CompareBranchesParams>();
  const url = urls.matchedUrlFromMatch(match);
  const source = decodeURIComponent(match.params.sourceName);
  const target = decodeURIComponent(match.params.targetName);

  return (
    <Switch>
      <Redirect exact from={url} to={`${url}/diff/`} />
      <Route path={`${baseUrl}/:sourceType/:sourceName/:targetType/:targetName/diff/`}>
        <LoadingDiff url={createDiffUrl(repository, source, target) + "?format=GIT"} stickyHeader={true} />
      </Route>
      <Route path={`${baseUrl}/:sourceType/:sourceName/:targetType/:targetName/changesets/`} exact>
        <IncomingChangesets repository={repository} source={source} target={target} url={`${url}/changesets`} />
      </Route>
      <Route path={`${baseUrl}/:sourceType/:sourceName/:targetType/:targetName/changesets/:page`} exact>
        <IncomingChangesets repository={repository} source={source} target={target} url={`${url}/changesets`} />
      </Route>
    </Switch>
  );
};

const CompareView: FC<Props> = ({ repository, baseUrl }) => {
  const [t] = useTranslation("repos");

  if (!repository._links.incomingDiff) {
    return null;
  }

  return (
    <>
      <Subtitle subtitle={t("compare.title")} />
      <CompareSelectBar repository={repository} baseUrl={baseUrl} />
      <CompareTabs baseUrl={baseUrl} />
      <CompareRoutes repository={repository} baseUrl={baseUrl} />
    </>
  );
};

export default CompareView;
