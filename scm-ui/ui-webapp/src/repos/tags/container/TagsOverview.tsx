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
import { ErrorNotification, Loading, Notification, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import orderTags from "../orderTags";
import TagTable from "../components/TagTable";
import { useTags } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const TagsOverview: FC<Props> = ({ repository, baseUrl }) => {
  const { isLoading, error, data } = useTags(repository);
  const [t] = useTranslation("repos");

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  const tags = data?._embedded?.tags || [];
  orderTags(tags);

  return (
    <>
      <Subtitle subtitle={t("tags.overview.title")} />
      {tags.length > 0 ? (
        <TagTable repository={repository} baseUrl={baseUrl} tags={tags} />
      ) : (
        <Notification type="info">{t("tags.overview.noTags")}</Notification>
      )}
    </>
  );
};

export default TagsOverview;
