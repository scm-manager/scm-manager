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

import { ApiResult, fetchResourceFromLocationHeader, getResponseJson, useRequiredIndexLink } from "./base";
import { useMutation, useQuery } from "react-query";
import { apiClient } from "./apiclient";
import { Repository, RepositoryCreation, RepositoryType, RepositoryUrlImport } from "@scm-manager/ui-types";
import { requiredLink } from "./links";

export const useImportLog = (logId: string): ApiResult<string> => {
  const link = useRequiredIndexLink("importLog").replace("{logId}", logId);
  return useQuery<string, Error>(["importLog", logId], () => apiClient.get(link).then((response) => response.text()));
};

export const useImportRepositoryFromUrl = (repositoryType: RepositoryType) => {
  const url = requiredLink(repositoryType, "import", "url");
  const { isLoading, error, data, mutate } = useMutation<Repository, Error, RepositoryUrlImport>((repo) =>
    apiClient
      .post(url, repo, "application/vnd.scmm-repository+json;v=2")
      .then(fetchResourceFromLocationHeader)
      .then(getResponseJson)
  );

  return {
    isLoading,
    error,
    importRepositoryFromUrl: (repository: RepositoryUrlImport) => mutate(repository),
    importedRepository: data,
  };
};

const importRepository = (url: string, repository: RepositoryCreation, file: File, password?: string) => {
  return apiClient
    .postBinary(url, (formData) => {
      formData.append("bundle", file, file.name);
      formData.append("repository", JSON.stringify({ ...repository, password }));
    })
    .then(fetchResourceFromLocationHeader)
    .then(getResponseJson);
};

type ImportRepositoryFromBundleRequest = {
  repository: RepositoryCreation;
  file: File;
  compressed?: boolean;
  password?: string;
};

export const useImportRepositoryFromBundle = (repositoryType: RepositoryType) => {
  const url = requiredLink(repositoryType, "import", "bundle");
  const { isLoading, error, data, mutate } = useMutation<Repository, Error, ImportRepositoryFromBundleRequest>(
    ({ repository, file, compressed, password }) =>
      importRepository(compressed ? url + "?compressed=true" : url, repository, file, password)
  );

  return {
    isLoading,
    error,
    importRepositoryFromBundle: (repository: RepositoryCreation, file: File, compressed?: boolean, password?: string) =>
      mutate({
        repository,
        file,
        compressed,
        password,
      }),
    importedRepository: data,
  };
};

type ImportFullRepositoryRequest = {
  repository: RepositoryCreation;
  file: File;
  password?: string;
};

export const useImportFullRepository = (repositoryType: RepositoryType) => {
  const { isLoading, error, data, mutate } = useMutation<Repository, Error, ImportFullRepositoryRequest>(
    ({ repository, file, password }) =>
      importRepository(requiredLink(repositoryType, "import", "fullImport"), repository, file, password)
  );

  return {
    isLoading,
    error,
    importFullRepository: (repository: RepositoryCreation, file: File, password?: string) =>
      mutate({
        repository,
        file,
        password,
      }),
    importedRepository: data,
  };
};
