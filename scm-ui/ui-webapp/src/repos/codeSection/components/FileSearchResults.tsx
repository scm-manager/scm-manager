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
import { Icon, Notification, urls } from "@scm-manager/ui-components";
import styled from "styled-components";
import { Link } from "react-router-dom";
import { Trans, useTranslation } from "react-i18next";

type Props = {
  paths: string[];
  query: string;
  contentBaseUrl: string;
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

type PathResultRowProps = {
  contentBaseUrl: string;
  path: string;
};

const PathResultRow: FC<PathResultRowProps> = ({ contentBaseUrl, path }) => {
  const [t] = useTranslation("repos");
  const link = urls.concat(contentBaseUrl, path);
  return (
    <tr>
      <IconColumn>
        <Link to={link}>
          <Icon title={t("fileSearch.file")} name="file" color="inherit" />
        </Link>
      </IconColumn>
      <LeftOverflowTd>
        <Link title={path} to={link} data-testid="file_search_single_result">
          {path}
        </Link>
      </LeftOverflowTd>
    </tr>
  );
};

type ResultTableProps = {
  contentBaseUrl: string;
  paths: string[];
};

const ResultTable: FC<ResultTableProps> = ({ contentBaseUrl, paths }) => (
  <table className="table table-hover table-sm is-fullwidth">
    <tbody>
      {paths.map(path => (
        <PathResultRow contentBaseUrl={contentBaseUrl} path={path} />
      ))}
    </tbody>
  </table>
);

const FileSearchResults: FC<Props> = ({ query, contentBaseUrl, paths = [] }) => {
  const [t] = useTranslation("repos");
  let body;
  if (query.length <= 1) {
    body = (
      <Notification className="m-4" type="info">
        {t("fileSearch.notifications.queryToShort")}
      </Notification>
    );
  } else if (paths.length === 0) {
    const queryCmp = <strong>{query}</strong>;
    body = (
      <Notification className="m-4" type="info">
        <Trans i18nKey="repos:fileSearch.notifications.emptyResult" values={{ query }} components={[queryCmp]} />
      </Notification>
    );
  } else {
    body = <ResultTable contentBaseUrl={contentBaseUrl} paths={paths} />;
  }
  return <div className="panel-block">{body}</div>;
};

export default FileSearchResults;
