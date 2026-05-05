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

import React, { FC, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useTags } from "@scm-manager/ui-api";
import { ErrorNotification, Loading, Notification, Subtitle } from "@scm-manager/ui-components";
import {
  Select,
  useDocumentTitleForRepository,
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  REPOSITORY_SUB_ROUTES,
} from "@scm-manager/ui-core";
import TagTable from "../components/TagTable";
import orderTags, { SORT_OPTIONS, SortOption } from "../orderTags";

const TagsOverview: FC = () => {
  const repository = useRepositoryFromParams();
  const baseUrl = useCurrentRepositoryBaseUrl();
  const { isLoading, error, data } = useTags(repository);
  const [t] = useTranslation("repos");
  useDocumentTitleForRepository(repository, t("tags.overview.title"));
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
            <option value={sortOption} key={sortOption}>
              {t(`tags.overview.sort.option.${sortOption}`)}
            </option>
          ))}
        </Select>
      </div>
      {tags.length > 0 ? (
        <TagTable repository={repository} baseUrl={`${baseUrl}/${REPOSITORY_SUB_ROUTES.TAG}`} tags={tags} />
      ) : (
        <Notification type="info">{t("tags.overview.noTags")}</Notification>
      )}
    </>
  );
};

export default TagsOverview;
