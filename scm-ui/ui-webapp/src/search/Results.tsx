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
import { QueryResult } from "@scm-manager/ui-types";
import Hits from "./Hits";
import { LinkPaginator } from "@scm-manager/ui-components";
import { Redirect, useLocation } from "react-router-dom";

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
      <div className="panel">
        <Hits type={type} hits={hits} />
      </div>
      <LinkPaginator collection={result} page={page} filter={query} />
    </>
  );
};

export default Results;
