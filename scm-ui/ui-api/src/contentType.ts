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
import { apiClient } from "./apiclient";
import { useQuery } from "react-query";
import { ApiResultWithFetching } from "./base";
import type { ContentType } from "@scm-manager/ui-types";
import { UseQueryOptions } from "react-query/types/react/types";

export type { ContentType } from "@scm-manager/ui-types";

function getContentType(url: string): Promise<ContentType> {
  return apiClient.head(url).then((response) => {
    return {
      type: response.headers.get("Content-Type") || "application/octet-stream",
      language: response.headers.get("X-Programming-Language") || undefined,
      aceMode: response.headers.get("X-Syntax-Mode-Ace") || undefined,
      codemirrorMode: response.headers.get("X-Syntax-Mode-Codemirror") || undefined,
      prismMode: response.headers.get("X-Syntax-Mode-Prism") || undefined,
    };
  });
}

export const useContentType = (
  url: string,
  options: Pick<UseQueryOptions<ContentType, Error>, "enabled"> = {}
): ApiResultWithFetching<ContentType> => {
  const { isLoading, isFetching, error, data } = useQuery<ContentType, Error>(
    ["contentType", url],
    () => getContentType(url),
    options
  );
  return {
    isLoading,
    isFetching,
    error,
    data,
  };
};
