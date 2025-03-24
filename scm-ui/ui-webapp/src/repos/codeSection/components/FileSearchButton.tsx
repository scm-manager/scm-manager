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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { File, Repository } from "@scm-manager/ui-types";
import { Link } from "react-router-dom";
import styled from "styled-components";
import { getFileSearchLink } from "../utils/fileSearchLink";
import { Icon } from "@scm-manager/ui-core";

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
  return (
    <Link
      to={getFileSearchLink(repository, revision, baseUrl, currentSource)}
      aria-label={t("fileSearch.button.title")}
      data-testid="file_search_button"
    >
      <SearchIcon title={t("fileSearch.button.title")}>search</SearchIcon>
    </Link>
  );
};

export default FileSearchButton;
