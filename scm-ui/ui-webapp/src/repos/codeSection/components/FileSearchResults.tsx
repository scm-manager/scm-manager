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
import { FileSearchHit } from "./FileSearchHit";
import { KeyboardIterator } from "@scm-manager/ui-core";
type Props = {
  paths: string[];
  contentBaseUrl: string;
};

type ResultTableProps = {
  contentBaseUrl: string;
  paths: string[];
};

const ResultTable: FC<ResultTableProps> = ({ contentBaseUrl, paths }) => {
  return (
    <table className="table table-hover table-sm is-fullwidth">
      <KeyboardIterator>
        <tbody>
          {paths.map((path, index) => (
            <FileSearchHit contentBaseUrl={contentBaseUrl} path={path} key={path} expectedIndex={index} />
          ))}
        </tbody>
      </KeyboardIterator>
    </table>
  );
};

const FileSearchResults: FC<Props> = ({ contentBaseUrl, paths = [] }) => {
  return <ResultTable contentBaseUrl={contentBaseUrl} paths={paths} />;
};

export default FileSearchResults;
