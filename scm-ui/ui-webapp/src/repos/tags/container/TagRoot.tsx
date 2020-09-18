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

import React, { FC, useEffect, useState } from "react";
import { Link, Repository, Tag } from "@scm-manager/ui-types";
import { Redirect, Switch, useLocation, useRouteMatch, Route } from "react-router-dom";
import { apiClient, ErrorNotification, Loading } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import TagView from "../components/TagView";
import { urls } from "@scm-manager/ui-components";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const TagRoot: FC<Props> = ({ repository, baseUrl }) => {
  const match = useRouteMatch();
  const [tags, setTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [tag, setTag] = useState<Tag>();

  useEffect(() => {
    const link = (repository._links?.tags as Link)?.href;
    if (link) {
      apiClient
        .get(link)
        .then(r => r.json())
        .then(r => setTags(r._embedded.tags))
        .catch(setError);
    }
  }, [repository]);

  useEffect(() => {
    const tagName = decodeURIComponent(match?.params?.tag);
    const link = tags?.length > 0 && (tags.find(tag => tag.name === tagName)?._links.self as Link).href;
    if (link) {
      apiClient
        .get(link)
        .then(r => r.json())
        .then(setTag)
        .then(() => setLoading(false))
        .catch(setError);
    }
  }, [tags]);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading || !tags) {
    return <Loading />;
  }

  const url = urls.matchedUrlFromMatch(match);

  return (
    <Switch>
      <Redirect exact from={url} to={`${url}/info`} />
      <Route path={`${url}/info`} component={() => <TagView repository={repository} tag={tag} />} />
    </Switch>
  );
};

export default TagRoot;
