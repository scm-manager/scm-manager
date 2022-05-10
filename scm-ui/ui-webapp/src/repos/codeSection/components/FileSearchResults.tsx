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
      {paths.map((path) => (
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
