import { ApiResult, useIndexJsonResource, useRequiredIndexLink } from "./base";
import { RepositoryRole, RepositoryRoleCollection, RepositoryRoleCreation } from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient, urls } from "@scm-manager/ui-components";
import { createQueryString } from "./utils";
import { requiredLink } from "./links";

export type UseRepositoryRolesRequest = {
  page?: number | string;
};

export const useRepositoryRoles = (request?: UseRepositoryRolesRequest): ApiResult<RepositoryRoleCollection> => {
  const queryClient = useQueryClient();
  const indexLink = useRequiredIndexLink("repositoryRoles");

  const queryParams: Record<string, string> = {};
  if (request?.page) {
    queryParams.page = request.page.toString();
  }

  return useQuery<RepositoryRoleCollection, Error>(
    ["repositoryRoles", request?.page || 0],
    () => apiClient.get(`${indexLink}?${createQueryString(queryParams)}`).then(response => response.json()),
    {
      onSuccess: (repositoryRoles: RepositoryRoleCollection) => {
        repositoryRoles._embedded.repositoryRoles.forEach((repositoryRole: RepositoryRole) =>
          queryClient.setQueryData(["repositoryRole", repositoryRole.name], repositoryRole)
        );
      }
    }
  );
};

export const useRepositoryRole = (name: string): ApiResult<RepositoryRole> => {
  const indexLink = useRequiredIndexLink("repositoryRoles");
  return useQuery<RepositoryRole, Error>(["repositoryRole", name], () =>
    apiClient.get(urls.concat(indexLink, name)).then(response => response.json())
  );
};

const createRepositoryRole = (link: string) => {
  return (repositoryRole: RepositoryRoleCreation) => {
    return apiClient
      .post(link, repositoryRole, "application/vnd.scmm-repositoryRole+json;v=2")
      .then(response => {
        const location = response.headers.get("Location");
        if (!location) {
          throw new Error("Server does not return required Location header");
        }
        return apiClient.get(location);
      })
      .then(response => response.json());
  };
};

export const useCreateRepositoryRole = () => {
  const queryClient = useQueryClient();
  const link = useRequiredIndexLink("repositoryRoles");
  const { mutate, data, isLoading, error } = useMutation<RepositoryRole, Error, RepositoryRoleCreation>(
    createRepositoryRole(link),
    {
      onSuccess: repositoryRole => {
        queryClient.setQueryData(["repositoryRole", repositoryRole.name], repositoryRole);
        return queryClient.invalidateQueries(["repositoryRoles"]);
      }
    }
  );
  return {
    create: (repositoryRole: RepositoryRoleCreation) => mutate(repositoryRole),
    isLoading,
    error,
    repositoryRole: data
  };
};

export const useUpdateRepositoryRole = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, RepositoryRole>(
    repositoryRole => {
      const updateUrl = requiredLink(repositoryRole, "update");
      return apiClient.put(updateUrl, repositoryRole, "application/vnd.scmm-repositoryRole+json;v=2");
    },
    {
      onSuccess: async (_, repositoryRole) => {
        await queryClient.invalidateQueries(["repositoryRole", repositoryRole.name]);
        await queryClient.invalidateQueries(["repositoryRoles"]);
      }
    }
  );
  return {
    update: (repositoryRole: RepositoryRole) => mutate(repositoryRole),
    isLoading,
    error,
    isUpdated: !!data
  };
};

export const useDeleteRepositoryRole = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, RepositoryRole>(
    repositoryRole => {
      const deleteUrl = requiredLink(repositoryRole, "delete");
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: async (_, name) => {
        await queryClient.invalidateQueries(["repositoryRole", name]);
        await queryClient.invalidateQueries(["repositoryRoles"]);
      }
    }
  );
  return {
    remove: (repositoryRole: RepositoryRole) => mutate(repositoryRole),
    isLoading,
    error,
    isDeleted: !!data
  };
};

export const useAvailableRepositoryVerbs = () => {
  const { data, isLoading, error } = useIndexJsonResource<{ verbs: string[] }>("repositoryVerbs");
  return { error, isLoading, data: data?.verbs };
};
