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
import { useMutation, useQuery, useQueryClient } from "react-query";
import { Group, GroupCollection, GroupCreation, Link } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import { createQueryString } from "./utils";
import { concat } from "./urls";

export type UseGroupsRequest = {
  page?: number | string;
  search?: string;
};

export const useGroups = (request?: UseGroupsRequest): ApiResult<GroupCollection> => {
  const queryClient = useQueryClient();
  const indexLink = useRequiredIndexLink("groups");

  const queryParams: Record<string, string> = {};
  if (request?.search) {
    queryParams.q = request.search;
  }
  if (request?.page) {
    queryParams.page = request.page.toString();
  }

  return useQuery<GroupCollection, Error>(
    ["groups", request?.search || "", request?.page || 0],
    () => apiClient.get(`${indexLink}?${createQueryString(queryParams)}`).then((response) => response.json()),
    {
      onSuccess: (groups: GroupCollection) => {
        groups._embedded.groups.forEach((group: Group) => queryClient.setQueryData(["group", group.name], group));
      },
    }
  );
};

export const useGroup = (name: string): ApiResult<Group> => {
  const indexLink = useRequiredIndexLink("groups");
  return useQuery<Group, Error>(["group", name], () =>
    apiClient.get(concat(indexLink, name)).then((response) => response.json())
  );
};

const createGroup = (link: string) => {
  return (group: GroupCreation) => {
    return apiClient
      .post(link, group, "application/vnd.scmm-group+json;v=2")
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

export const useCreateGroup = () => {
  const queryClient = useQueryClient();
  const link = useRequiredIndexLink("groups");
  const { mutate, data, isLoading, error } = useMutation<Group, Error, GroupCreation>(createGroup(link), {
    onSuccess: (group) => {
      queryClient.setQueryData(["group", group.name], group);
      return queryClient.invalidateQueries(["groups"]);
    },
  });
  return {
    create: (group: GroupCreation) => mutate(group),
    isLoading,
    error,
    group: data,
  };
};

export const useUpdateGroup = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Group>(
    (group) => {
      const updateUrl = (group._links.update as Link).href;
      return apiClient.put(updateUrl, group, "application/vnd.scmm-group+json;v=2");
    },
    {
      onSuccess: async (_, group) => {
        await queryClient.invalidateQueries(["group", group.name]);
        await queryClient.invalidateQueries(["groups"]);
      },
    }
  );
  return {
    update: (group: Group) => mutate(group),
    isLoading,
    error,
    isUpdated: !!data,
  };
};

export const useDeleteGroup = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Group>(
    (group) => {
      const deleteUrl = (group._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: async (_, name) => {
        await queryClient.removeQueries(["group", name]);
        await queryClient.invalidateQueries(["groups"]);
      },
    }
  );
  return {
    remove: (group: Group) => mutate(group),
    isLoading,
    error,
    isDeleted: !!data,
  };
};
