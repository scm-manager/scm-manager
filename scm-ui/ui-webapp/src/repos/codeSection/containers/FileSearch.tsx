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
import { Link, useHistory, useLocation, useParams } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Branch, Repository } from "@scm-manager/ui-types";
import { urls, usePaths } from "@scm-manager/ui-api";
import { createA11yId } from "@scm-manager/ui-components";
import { ErrorNotification, Icon, Loading, Notification, InputField, useDocumentTitle } from "@scm-manager/ui-core";
import CodeActionBar from "../components/CodeActionBar";
import FileSearchResults from "../components/FileSearchResults";
import { filepathSearch } from "../utils/filepathSearch";
import { encodeFilePath } from "../../sources/components/content/FileLink";

type Props = {
  repository: Repository;
  baseUrl: string;
  branches?: Branch[];
  selectedBranch?: string;
};

type Params = {
  revision: string;
};

const HomeLink = styled(Link)`
  border-right: 1px solid lightgray;
`;

const HomeIcon = styled(Icon)`
  line-height: 1.5rem;
`;

const useRevision = () => {
  const { revision } = useParams<Params>();
  return revision;
};

const FileSearch: FC<Props> = ({ repository, baseUrl, branches, selectedBranch }) => {
  const revision = useRevision();
  const location = useLocation();
  const history = useHistory();
  const { isLoading, error, data } = usePaths(repository, revision);
  const query = urls.getQueryStringFromLocation(location) || "";
  const prevSourcePath = urls.getPrevSourcePathFromLocation(location) || "";
  const [t] = useTranslation("repos");

  useDocumentTitle(
    t("fileSearch.searchWithRevisionAndNamespaceName", {
      revision: decodeURIComponent(revision),
      namespace: repository.namespace,
      name: repository.name,
    })
  );
  const [firstSelectedBranch] = useState<string | undefined>(selectedBranch);

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
      history.push(`${location.pathname}?q=${encodeURIComponent(query)}`);
    } else {
      history.push(`${location.pathname}?q=${encodeURIComponent(query)}&${prevSourceQuery}`);
    }
  };

  const onSelectBranch = (branch?: Branch) => {
    if (branch) {
      const prevSourceQuery = urls.createPrevSourcePathQuery(prevSourcePath);
      if (prevSourceQuery) {
        history.push(`${baseUrl}/search/${encodeURIComponent(branch.name)}?q=${query}&${prevSourceQuery}`);
      } else {
        history.push(`${baseUrl}/search/${encodeURIComponent(branch.name)}?q=${query}`);
      }
    }
  };

  const evaluateSwitchViewLink = (type: string) => {
    if (type === "sources" && repository.type !== "svn") {
      return `${baseUrl}/sources/${revision}/${encodeFilePath(prevSourcePath)}`;
    }

    if (type === "sources" && repository.type === "svn") {
      return `${baseUrl}/sources/${encodeFilePath(prevSourcePath)}`;
    }

    if (repository.type !== "svn") {
      return `${baseUrl}/branch/${revision}/changesets/${
        prevSourcePath ? `?${urls.createPrevSourcePathQuery(prevSourcePath)}` : ""
      }`;
    }

    if (prevSourcePath) {
      return `${baseUrl}/changesets/${urls.createPrevSourcePathQuery(prevSourcePath)}`;
    } else {
      return `${baseUrl}/changesets/${prevSourcePath}`;
    }
  };

  const contentBaseUrl = `${baseUrl}/sources/${revision}/`;

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

  return (
    <>
      <CodeActionBar
        branches={branches}
        selectedBranch={selectedBranch}
        onSelectBranch={onSelectBranch}
        switchViewLink={evaluateSwitchViewLink}
      />
      <div className="panel">
        <div
          className={classNames(
            "is-flex",
            "is-justify-content-flex-start",
            "is-align-items-center",
            "pt-4",
            "mx-3",
            "px-4",
            "pb-0"
          )}
        >
          <HomeLink
            className={classNames("mr-3", "pr-3")}
            aria-label={t("fileSearch.home")}
            to={firstSelectedBranch !== selectedBranch ? contentBaseUrl : () => evaluateSwitchViewLink("sources")}
          >
            <HomeIcon title={t("fileSearch.home")} color="inherit">
              {firstSelectedBranch !== selectedBranch ? "home" : "arrow-left"}
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
