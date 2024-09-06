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
