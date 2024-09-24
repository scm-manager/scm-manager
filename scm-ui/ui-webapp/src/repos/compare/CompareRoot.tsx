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
        <Redirect
          from={url}
          to={`${url}/b/${encodeURIComponent(data._embedded.branches.filter((b) => b.defaultBranch)[0].name)}`}
        />
      )}
    </Switch>
  );
};

export default CompareRoot;
