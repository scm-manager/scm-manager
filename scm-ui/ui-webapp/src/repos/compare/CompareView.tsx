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
