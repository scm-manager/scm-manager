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
import { useMutation, useQuery, useQueryClient } from "react-query";
import { Group, GroupCollection, GroupCreation, Link } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import * as urls from "./urls";
import { createQueryString } from "./utils";

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
    () => apiClient.get(`${indexLink}?${createQueryString(queryParams)}`).then(response => response.json()),
    {
      onSuccess: (groups: GroupCollection) => {
        groups._embedded.groups.forEach((group: Group) => queryClient.setQueryData(["group", group.name], group));
      }
    }
  );
};

export const useGroup = (name: string): ApiResult<Group> => {
  const indexLink = useRequiredIndexLink("groups");
  return useQuery<Group, Error>(["group", name], () =>
    apiClient.get(urls.concat(indexLink, name)).then(response => response.json())
  );
};

const createGroup = (link: string) => {
  return (group: GroupCreation) => {
    return apiClient
      .post(link, group, "application/vnd.scmm-group+json;v=2")
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

export const useCreateGroup = () => {
  const queryClient = useQueryClient();
  const link = useRequiredIndexLink("groups");
  const { mutate, data, isLoading, error } = useMutation<Group, Error, GroupCreation>(createGroup(link), {
    onSuccess: group => {
      queryClient.setQueryData(["group", group.name], group);
      return queryClient.invalidateQueries(["groups"]);
    }
  });
  return {
    create: (group: GroupCreation) => mutate(group),
    isLoading,
    error,
    group: data
  };
};

export const useUpdateGroup = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Group>(
    group => {
      const updateUrl = (group._links.update as Link).href;
      return apiClient.put(updateUrl, group, "application/vnd.scmm-group+json;v=2");
    },
    {
      onSuccess: async (_, group) => {
        await queryClient.invalidateQueries(["group", group.name]);
        await queryClient.invalidateQueries(["groups"]);
      }
    }
  );
  return {
    update: (group: Group) => mutate(group),
    isLoading,
    error,
    isUpdated: !!data
  };
};

export const useDeleteGroup = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, Group>(
    group => {
      const deleteUrl = (group._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: async (_, name) => {
        await queryClient.invalidateQueries(["group", name]);
        await queryClient.invalidateQueries(["groups"]);
      }
    }
  );
  return {
    remove: (group: Group) => mutate(group),
    isLoading,
    error,
    isDeleted: !!data
  };
};
