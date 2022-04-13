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
