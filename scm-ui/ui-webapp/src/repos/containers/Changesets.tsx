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

import React, { FC, useState } from "react";
import { useParams, Navigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useChangesets } from "@scm-manager/ui-api";
import { ChangesetCollection, Repository } from "@scm-manager/ui-types";
import { ChangesetList, LinkPaginator } from "@scm-manager/ui-components";
import {
  ErrorNotification,
  Notification,
  Loading,
  useDocumentTitle,
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  REPOSITORY_CODE_SUB_ROUTES,
  REPOSITORY_SUB_ROUTES,
  useSourceNameFromParams,
  InputField,
  DatePicker,
  useDebounce,
} from "@scm-manager/ui-core";
import { useSearchParams } from "react-router";

type ChangesetFilters = {
  message?: string;
  person?: string;
  before?: string;
  after?: string;
};
type ChangesetFilterKeys = keyof ChangesetFilters;

export const usePage = () => {
  const { page } = useParams<{ page?: string }>();

  if (page === "last") {
    return page;
  }

  // Parse the string from the URL to a number, defaulting to 1.
  return parseInt(page || "1", 10);
};

const Changesets: FC = ({ ...props }) => {
  const page = usePage();
  const repository = useRepositoryFromParams();
  const sourceName = useSourceNameFromParams();

  const [searchParams, setSearchParams] = useSearchParams();

  const [messageFilter, setMessageFilter] = useState(searchParams.get("message") || "");
  const debouncedMessageFilter = useDebounce(messageFilter, 500);

  const [personFilter, setPersonFilter] = useState(searchParams.get("person") || "");
  const debouncedPersonFilter = useDebounce(personFilter, 500);

  const [beforeFilter, setBeforeFilter] = useState(searchParams.get("before") || "");
  const debouncedBeforeFilter = useDebounce(beforeFilter, 500);

  const [afterFilter, setAfterFilter] = useState(searchParams.get("after") || "");
  const debouncedAfterFilter = useDebounce(afterFilter, 500);

  const [filterInteraction, setFilterInteraction] = useState(false);

  const { isLoading, isFetching, error, data } = useChangesets(repository, {
    revision: sourceName,
    page: typeof page === "number" ? page - 1 : -1,
    messageFilter: debouncedMessageFilter,
    personFilter: debouncedPersonFilter,
    beforeFilter: debouncedBeforeFilter,
    afterFilter: debouncedAfterFilter,
  });

  const [t] = useTranslation("repos");
  const hasActiveFilters = Boolean(messageFilter || personFilter || beforeFilter || afterFilter);
  const filtersPending =
    messageFilter !== debouncedMessageFilter ||
    personFilter !== debouncedPersonFilter ||
    beforeFilter !== debouncedBeforeFilter ||
    afterFilter !== debouncedAfterFilter;
  const changesets = data?._embedded?.changesets;
  const redirectingToValidPage = Boolean(
    data && data.pageTotal && (page === "last" || (typeof page === "number" && data.pageTotal < page && page > 1)),
  );

  const getFilterStatus = () => {
    if (!filterInteraction) return "";
    if (filtersPending || isFetching || redirectingToValidPage) return t("changesets.filters.filtering");
    if (error) return "";
    if (!changesets || changesets.length === 0)
      return hasActiveFilters ? t("changesets.filters.noMatches") : t("changesets.filters.noChangesets");
    return t("changesets.filters.results", { count: changesets.length });
  };
  const getDocumentTitle = () => {
    if (data?.pageTotal && data.pageTotal > 1 && page) {
      if (sourceName) {
        return t("changesets.commitsWithPageRevisionAndNamespaceName", {
          page,
          total: data.pageTotal,
          revision: sourceName,
          namespace: repository.namespace,
          name: repository.name,
        });
      } else {
        return t("changesets.commitsWithPageAndNamespaceName", {
          page,
          total: data.pageTotal,
          namespace: repository.namespace,
          name: repository.name,
        });
      }
    } else if (sourceName) {
      return t("changesets.commitsWithRevisionAndNamespaceName", {
        revision: sourceName,
        namespace: repository.namespace,
        name: repository.name,
      });
    } else {
      return t("changesets.commitsWithNamespaceName", {
        namespace: repository.namespace,
        name: repository.name,
      });
    }
  };
  useDocumentTitle(getDocumentTitle());

  const updateFilters = (updatedParams: ChangesetFilters) => {
    const filters: ChangesetFilters = {
      message: messageFilter,
      person: personFilter,
      before: beforeFilter,
      after: afterFilter,
      ...updatedParams,
    };

    for (const [key, value] of Object.entries(filters)) {
      if (!value) {
        delete filters[key as ChangesetFilterKeys];
      }
    }

    setSearchParams(filters);
  };

  const formatDate = (date: Date): string => {
    const year = date.getFullYear().toString().padStart(4, "0");
    const month = (date.getMonth() + 1).toString().padStart(2, "0");
    const day = date.getDate().toString().padStart(2, "0");
    return `${year}-${month}-${day}`;
  };
  const today = formatDate(new Date());

  return (
    <>
      <fieldset
        className="field"
        aria-label={t("changesets.filters.label")}
        aria-description={t("changesets.filters.description")}
      >
        <div className="columns">
          <InputField
            className="column"
            label={t("changesets.filters.message")}
            value={messageFilter}
            type="search"
            onChange={(e) => {
              setFilterInteraction(true);
              setMessageFilter(e.target.value);
              updateFilters({ message: e.target.value });
            }}
          />
          <InputField
            className="column"
            label={t("changesets.filters.person")}
            value={personFilter}
            type="search"
            onChange={(e) => {
              setFilterInteraction(true);
              setPersonFilter(e.target.value);
              updateFilters({ person: e.target.value });
            }}
          />
          <div className="column columns">
            <DatePicker
              className="column"
              label={t("changesets.filters.after")}
              value={afterFilter}
              onChange={(value) => {
                setFilterInteraction(true);
                setAfterFilter(value);
                updateFilters({ after: value });
              }}
              min="2000-01-01"
              max={beforeFilter || today}
            />
            <DatePicker
              className="column"
              label={t("changesets.filters.before")}
              value={beforeFilter}
              onChange={(value) => {
                setFilterInteraction(true);
                setBeforeFilter(value);
                updateFilters({ before: value });
              }}
              min={afterFilter}
              max={today}
            />
          </div>
        </div>
      </fieldset>
      <div className="is-sr-only" role="status" aria-atomic="true">
        {getFilterStatus()}
      </div>
      <ChangesetsPanel
        isLoading={isLoading}
        error={error}
        data={data}
        repository={repository}
        sourceName={sourceName}
        hasActiveFilters={hasActiveFilters}
        {...props}
      />
    </>
  );
};

type ChangesetsPanelProps = {
  repository: Repository;
  sourceName?: string;
  error: Error | null;
  isLoading: boolean;
  data?: ChangesetCollection;
  hasActiveFilters?: boolean;
};

export const ChangesetsPanel: FC<ChangesetsPanelProps> = ({
  repository,
  error,
  isLoading,
  data,
  sourceName,
  hasActiveFilters = false,
}) => {
  const page = usePage();
  const [t] = useTranslation("repos");
  const changesets = data?._embedded?.changesets;
  const baseUrl = useCurrentRepositoryBaseUrl();
  const [searchParams] = useSearchParams();

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  //Checking whether user navigated to the last page or if provided page is outside of the results
  if (data && data.pageTotal && (page === "last" || (data.pageTotal < page && page > 1))) {
    return <Navigate to={{ pathname: `../${data.pageTotal}`, search: searchParams.toString() }} />;
  }

  if (!data || !changesets || changesets.length === 0) {
    return (
      <Notification type="info">
        {t(hasActiveFilters ? "changesets.noChangesetsMatchingFilters" : "changesets.noChangesets")}
      </Notification>
    );
  }

  const baseUrl1 = sourceName
    ? `${baseUrl.substring(1)}/${REPOSITORY_SUB_ROUTES.CODE}/${REPOSITORY_SUB_ROUTES.BRANCH}/${encodeURIComponent(sourceName)}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}`
    : `${baseUrl.substring(1)}/${REPOSITORY_SUB_ROUTES.CODE}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}`;
  return (
    <div className="panel">
      <div className="panel-block">
        <ChangesetList repository={repository} changesets={changesets} sourceName={sourceName} />
      </div>
      <div className="panel-footer">
        {/* As soon as this renders, page should always already be a number (The last possible page) */}
        <LinkPaginator collection={data} filter={searchParams} page={page as number} baseUrl={baseUrl1} />
      </div>
    </div>
  );
};

export default Changesets;
