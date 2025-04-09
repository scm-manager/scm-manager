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

import React from "react";
import { useTranslation } from "react-i18next";
import { urls } from "@scm-manager/ui-api";
import { Icon, useKeyboardIteratorTargetV2 } from "@scm-manager/ui-core";
import { Link } from "react-router-dom";
import styled from "styled-components";

type FileSearchHitProps = {
  contentBaseUrl: string;
  path: string;
  expectedIndex?: number;
};

const IconColumn = styled.td`
  width: 16px;
`;

const LeftOverflowTd = styled.td`
  overflow: hidden;
  max-width: 1px;
  white-space: nowrap;
  text-overflow: ellipsis;
  direction: rtl;
  text-align: left !important;
`;

export function FileSearchHit({ contentBaseUrl, path, expectedIndex }: FileSearchHitProps) {
  const [t] = useTranslation("repos");
  const link = urls.concat(contentBaseUrl, path);
  const ref = useKeyboardIteratorTargetV2({ expectedIndex: expectedIndex ?? 0 });
  return (
    <tr>
      <IconColumn aria-hidden="true">
        <Icon title={t("fileSearch.file")}>file</Icon>
      </IconColumn>
      <LeftOverflowTd>
        <Link title={path} to={link} data-testid="file_search_single_result" ref={ref}>
          {path}
        </Link>
      </LeftOverflowTd>
    </tr>
  );
}
