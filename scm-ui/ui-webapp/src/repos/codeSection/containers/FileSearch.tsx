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
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { urls, useBranchesIfAvailable, usePaths } from "@scm-manager/ui-api";
import { createA11yId } from "@scm-manager/ui-components";
import {
  ErrorNotification,
  Icon,
  Loading,
  Notification,
  InputField,
  useDocumentTitle,
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  useSourceNameFromParams,
  REPOSITORY_SUB_ROUTES,
  REPOSITORY_CODE_SUB_ROUTES,
} from "@scm-manager/ui-core";
import CodeActionBar from "../components/CodeActionBar";
import FileSearchResults from "../components/FileSearchResults";
import { filepathSearch } from "../utils/filepathSearch";
import { encodeFilePath } from "../../sources/components/content/FileLink";

const HomeLink = styled(Link)`
  border-right: 1px solid lightgray;
`;

const HomeIcon = styled(Icon)`
  line-height: 1.5rem;
`;

const FileSearch: FC = () => {
  const repository = useRepositoryFromParams();
  const baseUrl = useCurrentRepositoryBaseUrl() + "/" + REPOSITORY_SUB_ROUTES.CODE;
  const { data: branchesData } = useBranchesIfAvailable(repository);
  const sourceName = useSourceNameFromParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { isLoading, error, data } = usePaths(repository, sourceName);
  const query = urls.getQueryStringFromLocation(location) || "";
  const prevSourcePath = urls.getPrevSourcePathFromLocation(location) || "";
  const [t] = useTranslation("repos");

  if (!sourceName) {
    throw new Error("Source type or name missing in URL");
  }

  useDocumentTitle(
    t("fileSearch.searchWithRevisionAndNamespaceName", {
      revision: decodeURIComponent(sourceName),
      namespace: repository.namespace,
      name: repository.name,
    }),
  );
  const [firstSelectedBranch] = useState<string | undefined>(sourceName);

  let result: string[];

  if (data && query && query.length > 1) {
    result = filepathSearch(data.paths, query);
  } else {
    result = [];
  }

  const onSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.currentTarget.value;
    const prevSourceQuery = urls.createPrevSourcePathQuery(prevSourcePath);
    if (prevSourceQuery) {
      navigate(`${location.pathname}?q=${encodeURIComponent(query)}`);
    } else {
      navigate(`${location.pathname}?q=${encodeURIComponent(query)}&${prevSourceQuery}`);
    }
  };

  const onSelect = (sourceType: string, sourceName: string) => {
    if (sourceName) {
      const prevSourceQuery = urls.createPrevSourcePathQuery(prevSourcePath);
      let searchString = `?q=${query}`;
      if (prevSourceQuery) {
        searchString += `&${prevSourceQuery}`;
      }
      navigate(`${baseUrl}/${REPOSITORY_SUB_ROUTES.SEARCH}/${encodeURIComponent(sourceName)}${searchString}`);
    }
  };

  const evaluateSwitchViewLink = (viewType: string) => {
    if (viewType === "sources" && repository.type !== "svn") {
      return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${encodeURIComponent(sourceName)}/${encodeFilePath(prevSourcePath)}`;
    }

    if (viewType === "sources" && repository.type === "svn") {
      return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${encodeFilePath(prevSourcePath)}`;
    }

    if (repository.type !== "svn") {
      return `${baseUrl}/${encodeURIComponent(sourceName)}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/${
        prevSourcePath ? `?${urls.createPrevSourcePathQuery(prevSourcePath)}` : ""
      }`;
    }

    if (prevSourcePath) {
      return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/?${urls.createPrevSourcePathQuery(prevSourcePath)}`;
    } else {
      return `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}/`;
    }
  };

  const contentBaseUrl = `${baseUrl}/${REPOSITORY_CODE_SUB_ROUTES.SOURCES}/${encodeURIComponent(sourceName)}/`;

  const fileSearchDescriptionId = createA11yId("fileSearchDescription");
  let body;

  if (query.length <= 1) {
    body = (
      <Notification className="m-4" type="info" hidden={query.length > 1}>
        <p id={fileSearchDescriptionId}>{t("fileSearch.input.help")}</p>
      </Notification>
    );
  } else if (!isLoading && result.length === 0) {
    const queryCmp = <strong>{query}</strong>;
    body = (
      <p className="mt-3" id={fileSearchDescriptionId}>
        <Trans i18nKey="repos:fileSearch.notifications.emptyResult" values={{ query }} components={[queryCmp]} />
      </p>
    );
  } else {
    body = (
      <>
        <p
          className="pl-4 has-text-weight-semibold"
          hidden={query.length <= 1}
          id={fileSearchDescriptionId}
          aria-hidden={true}
        >
          {t("fileSearch.results", { count: result.length })}
        </p>
        <FileSearchResults contentBaseUrl={contentBaseUrl} paths={result} />
      </>
    );
  }

  const hasContent =
    !repository._links.branches || (branchesData?._embedded?.branches && branchesData._embedded.branches.length > 0);
  return (
    <>
      <CodeActionBar hasContent={hasContent} onSelect={onSelect} switchViewLink={evaluateSwitchViewLink} />
      <div className="panel">
        <div
          className={classNames(
            "is-flex",
            "is-justify-content-flex-start",
            "is-align-items-center",
            "pt-4",
            "mx-3",
            "px-4",
            "pb-0",
          )}
        >
          <HomeLink
            className={classNames("mr-3", "pr-3")}
            aria-label={t("fileSearch.home")}
            to={firstSelectedBranch !== sourceName ? contentBaseUrl : evaluateSwitchViewLink("sources")}
          >
            <HomeIcon title={t("fileSearch.home")} color="inherit">
              {firstSelectedBranch !== sourceName ? "home" : "arrow-left"}
            </HomeIcon>
          </HomeLink>
          <InputField
            autoFocus={true}
            placeholder={t("fileSearch.input.placeholder")}
            className="is-full-width is-flex pr-2 ml-2"
            label=""
            defaultValue={query}
            aria-describedby={fileSearchDescriptionId}
            testId="file_search_filter_input"
            icon="fas fa-search"
            onChange={onSearch}
          />
        </div>
        <ErrorNotification error={error} />
        <div className="panel-block">{isLoading ? <Loading /> : body}</div>
      </div>
    </>
  );
};

export default FileSearch;
