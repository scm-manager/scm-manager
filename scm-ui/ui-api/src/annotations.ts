import { AnnotatedSource, File, Link, Repository } from "@scm-manager/ui-types";
import { useQuery } from "react-query";
import { apiClient } from "./apiclient";
import { ApiResult } from "./base";
import { repoQueryKey } from "./keys";

export const useAnnotations = (repository: Repository, file: File): ApiResult<AnnotatedSource> => {
  const { isLoading, error, data } = useQuery<AnnotatedSource, Error>(
    repoQueryKey(repository, "annotations", file.revision),
    () => apiClient.get((file._links.annotate as Link).href).then((response) => response.json())
  );
  return {
    isLoading,
    error,
    data,
  };
};
