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
import { QueryResult } from "@scm-manager/ui-types";
import Hits from "./Hits";
import { LinkPaginator } from "@scm-manager/ui-components";
import { Redirect, useLocation } from "react-router-dom";
import { CardListBox } from "@scm-manager/ui-layout";
import { KeyboardIterator } from "@scm-manager/ui-shortcuts";

type Props = {
  result: QueryResult;
  type: string;
  page: number;
  query: string;
};

const Results: FC<Props> = ({ result, type, page, query }) => {
  const location = useLocation();
  const hits = result?._embedded?.hits;

  let pathname = location.pathname;
  if (!pathname.endsWith("/")) {
    pathname = pathname.substring(0, pathname.lastIndexOf("/") + 1);
  }

  if (result && result.pageTotal < page && page > 1) {
    return <Redirect to={`${pathname}${result.pageTotal}${location.search}`} />;
  }

  return (
    <>
      <CardListBox>
        <KeyboardIterator>
          <Hits type={type} hits={hits} />
        </KeyboardIterator>
      </CardListBox>
      <LinkPaginator collection={result} page={page} filter={query} />
    </>
  );
};

export default Results;
