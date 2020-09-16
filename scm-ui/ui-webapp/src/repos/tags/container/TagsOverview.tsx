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
import { Repository, Tag, Link } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Notification, Subtitle, apiClient } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import orderTags from "../orderTags";
import TagTable from "../components/TagTable";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const TagsOverview: FC<Props> = ({ repository, baseUrl }) => {
  const [t] = useTranslation("repos");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [tags, setTags] = useState<Tag[]>([]);

  useEffect(() => {
    const link = (repository._links?.tags as Link)?.href;
    if (link) {
      setLoading(true);
      apiClient
        .get(link)
        .then(r => r.json())
        .then(r => setTags(r._embedded.tags))
        .then(() => setLoading(false))
        .catch(setError);
    }
  }, [repository]);

  const renderTagsTable = () => {
    if (!loading && tags?.length > 0) {
     orderTags(tags);
      return <TagTable baseUrl={baseUrl} tags={tags} />;
    }
    return <Notification type="info">{t("tags.overview.noTags")}</Notification>;
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  return (
    <>
      <Subtitle subtitle={t("tags.overview.title")} />
      {renderTagsTable()}
    </>
  );
};

export default TagsOverview;
