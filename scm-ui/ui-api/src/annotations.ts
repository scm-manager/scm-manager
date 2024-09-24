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

import { AnnotatedSource, File, Link, Repository } from "@scm-manager/ui-types";
import { useQuery } from "react-query";
import { apiClient } from "./apiclient";
import { ApiResultWithFetching } from "./base";
import { repoQueryKey } from "./keys";

export const useAnnotations = (
  repository: Repository,
  revision: string,
  file: File
): ApiResultWithFetching<AnnotatedSource> => {
  const { isLoading, isFetching, error, data } = useQuery<AnnotatedSource, Error>(
    repoQueryKey(repository, "annotations", revision, file.path),
    () => apiClient.get((file._links.annotate as Link).href).then((response) => response.json())
  );
  return {
    isLoading,
    isFetching,
    error,
    data,
  };
};
