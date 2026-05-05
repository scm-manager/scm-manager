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

import { ApiResult, useRequiredIndexLink } from "./base";
import { RepositoryRole, RepositoryRoleCollection, RepositoryRoleCreation } from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiClient } from "./apiclient";
import * as urls from "./urls";
import { createQueryString } from "./utils";
import { requiredLink } from "./links";
import { useEffect } from "react";

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

  const result = useQuery<RepositoryRoleCollection, Error>({
    queryKey: ["repositoryRoles", request?.page || 0],
    queryFn: () => apiClient.get(`${indexLink}?${createQueryString(queryParams)}`).then((response) => response.json()),
  });
  useEffect(() => {
    if (result.data) {
      result.data._embedded?.repositoryRoles.forEach((repositoryRole: RepositoryRole) =>
        queryClient.setQueryData(["repositoryRole", repositoryRole.name], repositoryRole),
      );
    }
  }, [queryClient, result.data]);

  return result;
};

export const useRepositoryRole = (name: string): ApiResult<RepositoryRole> => {
  const indexLink = useRequiredIndexLink("repositoryRoles");
  return useQuery<RepositoryRole, Error>({
    queryKey: ["repositoryRole", name],
    queryFn: () => apiClient.get(urls.concat(indexLink, name)).then((response) => response.json()),
  });
};

const createRepositoryRole = (link: string) => {
  return (repositoryRole: RepositoryRoleCreation) => {
    return apiClient
      .post(link, repositoryRole, "application/vnd.scmm-repositoryRole+json;v=2")
      .then((response) => {
        const location = response.headers.get("Location");
        if (!location) {
          throw new Error("Server does not return required Location header");
        }
        return apiClient.get(location);
      })
      .then((response) => response.json());
  };
};

export const useCreateRepositoryRole = () => {
  const queryClient = useQueryClient();
  const link = useRequiredIndexLink("repositoryRoles");
  const { mutate, data, isPending, error } = useMutation<RepositoryRole, Error, RepositoryRoleCreation>({
    mutationFn: createRepositoryRole(link),
    onSuccess: (repositoryRole) => {
      queryClient.setQueryData(["repositoryRole", repositoryRole.name], repositoryRole);
      return queryClient.invalidateQueries({ queryKey: ["repositoryRoles"] });
    },
  });
  return {
    create: (repositoryRole: RepositoryRoleCreation) => mutate(repositoryRole),
    isPending,
    error,
    repositoryRole: data,
  };
};

export const useUpdateRepositoryRole = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, RepositoryRole>({
    mutationFn: (repositoryRole) => {
      const updateUrl = requiredLink(repositoryRole, "update");
      return apiClient.put(updateUrl, repositoryRole, "application/vnd.scmm-repositoryRole+json;v=2");
    },
    onSuccess: async (_, repositoryRole) => {
      await queryClient.invalidateQueries({ queryKey: ["repositoryRole", repositoryRole.name] });
      await queryClient.invalidateQueries({ queryKey: ["repositoryRoles"] });
    },
  });
  return {
    update: (repositoryRole: RepositoryRole) => mutate(repositoryRole),
    isPending,
    error,
    isUpdated: !!data,
  };
};

export const useDeleteRepositoryRole = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, RepositoryRole>({
    mutationFn: (repositoryRole) => {
      const deleteUrl = requiredLink(repositoryRole, "delete");
      return apiClient.delete(deleteUrl);
    },
    onSuccess: async (_, name) => {
      await queryClient.removeQueries({ queryKey: ["repositoryRole", name] });
      await queryClient.invalidateQueries({ queryKey: ["repositoryRoles"] });
    },
  });
  return {
    remove: (repositoryRole: RepositoryRole) => mutate(repositoryRole),
    isPending,
    error,
    isDeleted: !!data,
  };
};
