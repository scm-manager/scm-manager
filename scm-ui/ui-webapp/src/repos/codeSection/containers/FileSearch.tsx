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
import { Branch, Repository } from "@scm-manager/ui-types";
import { Link, useHistory, useLocation, useParams } from "react-router-dom";
import { urls, usePaths } from "@scm-manager/ui-api";
import { ErrorNotification, FilterInput, Help, Icon, Loading } from "@scm-manager/ui-components";
import CodeActionBar from "../components/CodeActionBar";
import styled from "styled-components";
import FileSearchResults from "../components/FileSearchResults";
import { useTranslation } from "react-i18next";
import { filepathSearch } from "../utils/filepathSearch";

type Props = {
  repository: Repository;
  baseUrl: string;
  branches?: Branch[];
  selectedBranch?: string;
};

type Params = {
  revision: string;
};

const InputContainer = styled.div`
  padding: 1rem 1.75rem 0 1.75rem;
  display: flex;
  align-items: center;
  justify-content: flex-start;
`;

const HomeLink = styled(Link)`
  border-right: 1px solid lightgray;
  margin-right: 0.75rem;
  padding-right: 0.75em;
`;

const HomeIcon = styled(Icon)`
  line-height: 1.5rem;
`;

const SearchHelp = styled(Help)`
  margin-left: 0.75rem;
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
  const [t] = useTranslation("repos");
  useEffect(() => {
    if (query.length > 1 && data) {
      setResult(filepathSearch(data.paths, query));
    } else {
      setResult([]);
    }
  }, [data, query]);

  const search = (query: string) => {
    history.push(`${location.pathname}?q=${encodeURIComponent(query)}`);
  };

  const onSelectBranch = (branch?: Branch) => {
    if (branch) {
      history.push(`${baseUrl}/search/${encodeURIComponent(branch.name)}?q=${query}`);
    }
  };

  const evaluateSwitchViewLink = (type: string) => {
    if (type === "sources") {
      return `${baseUrl}/sources/${revision}/`;
    }
    return `${baseUrl}/changesets/${revision}/`;
  };

  const contentBaseUrl = `${baseUrl}/sources/${revision}/`;

  return (
    <>
      <CodeActionBar
        branches={branches}
        selectedBranch={selectedBranch}
        onSelectBranch={onSelectBranch}
        switchViewLink={evaluateSwitchViewLink}
      />
      <div className="panel">
        <InputContainer>
          <HomeLink to={contentBaseUrl}>
            <HomeIcon title={t("filesearch.home")} name="home" color="inherit" />
          </HomeLink>
          <FilterInput
            className="is-full-width"
            placeholder={t("filesearch.input.placeholder")}
            value={query}
            filter={search}
            autoFocus={true}
          />
          <SearchHelp message={t("filesearch.input.help")} />
        </InputContainer>
        <ErrorNotification error={error} />
        {isLoading ? <Loading /> : <FileSearchResults contentBaseUrl={contentBaseUrl} query={query} paths={result} />}
      </div>
    </>
  );
};

export default FileSearch;
