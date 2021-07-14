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

import { ApiResult, useRequiredIndexLink } from "./base";
import { QueryResult } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import { createQueryString } from "./utils";
import { useQuery } from "react-query";

export type SearchOptions = {
  page?: number;
  pageSize?: number;
};

const defaultSearchOptions: SearchOptions = {};

export const useSearch = (query: string, options = defaultSearchOptions): ApiResult<QueryResult> => {
  const link = useRequiredIndexLink("search");

  const queryParams: Record<string, string> = {};
  queryParams.q = query;
  if (options.page) {
    queryParams.page = options.page.toString();
  }
  if (options.pageSize) {
    queryParams.pageSize = options.pageSize.toString();
  }
  return useQuery<QueryResult, Error>(
    ["search", query],
    () => apiClient.get(`${link}?${createQueryString(queryParams)}`).then((response) => response.json()),
    {
      enabled: query.length > 1,
    }
  );
};
