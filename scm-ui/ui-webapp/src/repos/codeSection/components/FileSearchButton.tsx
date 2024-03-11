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
import { useTranslation } from "react-i18next";
import { File, Repository } from "@scm-manager/ui-types";
import { Link } from "react-router-dom";
import { Icon } from "@scm-manager/ui-components";
import styled from "styled-components";
import { urls } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  revision: string;
  baseUrl: string;
  currentSource: File;
};

const SearchIcon = styled(Icon)`
  line-height: 1.5rem;
`;

const FileSearchButton: FC<Props> = ({ baseUrl, revision, currentSource, repository }) => {
  const [t] = useTranslation("repos");
  const currentSourcePath =
    repository.type === "svn"
      ? urls.createPrevSourcePathQuery(`${revision}/${currentSource.path}`)
      : urls.createPrevSourcePathQuery(currentSource.path);

  return (
    <Link
      to={`${baseUrl}/search/${encodeURIComponent(revision)}?${currentSourcePath}`}
      aria-label={t("fileSearch.button.title")}
      data-testid="file_search_button"
    >
      <SearchIcon title={t("fileSearch.button.title")} name="search" color="inherit" />
    </Link>
  );
};

export default FileSearchButton;
