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

import React, { FC, useEffect, useState } from "react";
import { Link, useHistory, useLocation, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Branch, Repository } from "@scm-manager/ui-types";
import { urls, usePaths } from "@scm-manager/ui-api";
import { createA11yId, ErrorNotification, FilterInput, Help, Icon, Loading } from "@scm-manager/ui-components";
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
  const [result, setResult] = useState<string[]>([]);
  const query = urls.getQueryStringFromLocation(location) || "";
  const prevSourcePath = urls.getPrevSourcePathFromLocation(location) || "";
  const [t] = useTranslation("repos");
  const [firstSelectedBranch, setBranchChanged] = useState<string | undefined>(selectedBranch);

  useEffect(() => {
    if (query.length > 1 && data) {
      setResult(filepathSearch(data.paths, query));
    } else {
      setResult([]);
    }
  }, [data, query]);

  const search = (query: string) => {
    const prevSourceQuery = urls.createPrevSourcePathQuery(prevSourcePath);
    history.push(`${location.pathname}?q=${encodeURIComponent(query)}${prevSourceQuery ? `&${prevSourceQuery}` : ""}`);
  };

  const onSelectBranch = (branch?: Branch) => {
    if (branch) {
      const prevSourceQuery = urls.createPrevSourcePathQuery(prevSourcePath);
      history.push(
        `${baseUrl}/search/${encodeURIComponent(branch.name)}?q=${query}${prevSourceQuery ? `&${prevSourceQuery}` : ""}`
      );
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

    return `${baseUrl}/changesets/${prevSourcePath ? `?${urls.createPrevSourcePathQuery(prevSourcePath)}` : ""}`;
  };

  const contentBaseUrl = `${baseUrl}/sources/${revision}/`;
  const id = createA11yId("file-search");

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
            to={firstSelectedBranch !== selectedBranch ? contentBaseUrl : () => evaluateSwitchViewLink("sources")}
          >
            <HomeIcon
              title={t("fileSearch.home")}
              name={firstSelectedBranch !== selectedBranch ? "home" : "arrow-left"}
              color="inherit"
            />
          </HomeLink>
          <FilterInput
            className="is-full-width pr-2"
            placeholder={t("fileSearch.input.placeholder")}
            value={query}
            filter={search}
            autoFocus={true}
            id={id}
            testId="file_search_filter_input"
          />
          <Help message={t("fileSearch.input.help")} id={id} />
        </div>
        <ErrorNotification error={error} />
        {isLoading ? <Loading /> : <FileSearchResults contentBaseUrl={contentBaseUrl} query={query} paths={result} />}
      </div>
    </>
  );
};

export default FileSearch;
