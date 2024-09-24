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
