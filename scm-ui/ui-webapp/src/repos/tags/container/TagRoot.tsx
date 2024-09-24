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
import { Repository } from "@scm-manager/ui-types";
import { Redirect, Switch, useRouteMatch, Route } from "react-router-dom";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import TagView from "../components/TagView";
import { urls } from "@scm-manager/ui-components";
import { useTag } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  baseUrl: string;
};

type Params = {
  tag: string;
};

const TagRoot: FC<Props> = ({ repository, baseUrl }) => {
  const match = useRouteMatch<Params>();
  const { isLoading, error, data: tag } = useTag(repository, match.params.tag);
  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || !tag) {
    return <Loading />;
  }

  const url = urls.matchedUrlFromMatch(match);

  const escapedUrl = urls.escapeUrlForRoute(url);

  return (
    <Switch>
      <Redirect exact from={escapedUrl} to={`${url}/info`} />
      <Route path={`${escapedUrl}/info`}>
        <TagView repository={repository} tag={tag} />
      </Route>
    </Switch>
  );
};
export default TagRoot;
