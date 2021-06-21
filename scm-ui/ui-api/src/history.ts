import { ApiResult } from "./base";
import { Changeset, ChangesetCollection, File, Link, Repository } from "@scm-manager/ui-types";
import { useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { createQueryString } from "./utils";
import { changesetQueryKey } from "./changesets";
import { repoQueryKey } from "./keys";

export type UseHistoryRequest = {
  page?: number | string;
};

export const useHistory = (
  repository: Repository,
  file: File,
  request?: UseHistoryRequest
): ApiResult<ChangesetCollection> => {
  const queryClient = useQueryClient();
  const link = (file._links.history as Link).href;

  const queryParams: Record<string, string> = {};
  if (request?.page) {
    queryParams.page = request.page.toString();
  }

  return useQuery<ChangesetCollection, Error>(
    repoQueryKey(repository, "history", file.revision, request?.page || 0),
    () => apiClient.get(`${link}?${createQueryString(queryParams)}`).then((response) => response.json()),
    {
      keepPreviousData: true,
      onSuccess: (changesets: ChangesetCollection) => {
        changesets._embedded.changesets.forEach((changeset: Changeset) =>
          queryClient.setQueryData(changesetQueryKey(repository, changeset.id), changeset)
        );
      },
    }
  );
};
