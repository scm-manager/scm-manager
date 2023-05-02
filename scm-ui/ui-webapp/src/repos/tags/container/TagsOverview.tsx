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

import React, { FC, useMemo, useState } from "react";
import { Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Notification, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import orderTags, { SORT_OPTIONS, SortOption } from "../orderTags";
import TagTable from "../components/TagTable";
import { useTags } from "@scm-manager/ui-api";
import { Select } from "@scm-manager/ui-forms";

type Props = {
  repository: Repository;
  baseUrl: string;
};

const TagsOverview: FC<Props> = ({ repository, baseUrl }) => {
  const { isLoading, error, data } = useTags(repository);
  const [t] = useTranslation("repos");
  const [sort, setSort] = useState<SortOption | undefined>();
  const tags = useMemo(() => orderTags(data?._embedded?.tags || [], sort), [data, sort]);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  return (
    <>
      <Subtitle subtitle={t("tags.overview.title")} />
      <div className="is-flex is-align-items-center mb-3">
        <label className="mr-2" htmlFor="tags-overview-sort">
          {t("tags.overview.sort.label")}
        </label>
        <Select id="tags-overview-sort" onChange={(e) => setSort(e.target.value as SortOption)}>
          {SORT_OPTIONS.map((sortOption) => (
            <option value={sortOption}>{t(`tags.overview.sort.option.${sortOption}`)}</option>
          ))}
        </Select>
      </div>
      {tags.length > 0 ? (
        <TagTable repository={repository} baseUrl={baseUrl} tags={tags} />
      ) : (
        <Notification type="info">{t("tags.overview.noTags")}</Notification>
      )}
    </>
  );
};

export default TagsOverview;
