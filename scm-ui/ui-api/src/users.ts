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
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, Me, PermissionOverview, User, UserCollection, UserCreation } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import { createQueryString } from "./utils";
import { concat } from "./urls";
import { requiredLink } from "./links";
import { useEffect } from "react";

export type UseUsersRequest = {
  page?: number | string;
  search?: string;
};

export const useUsers = (request?: UseUsersRequest): ApiResult<UserCollection> => {
  const queryClient = useQueryClient();
  const indexLink = useRequiredIndexLink("users");

  const queryParams: Record<string, string> = {};
  if (request?.search) {
    queryParams.q = request.search;
  }
  if (request?.page) {
    queryParams.page = request.page.toString();
  }

  const result = useQuery<UserCollection, Error>({
    queryKey: ["users", request?.search || "", request?.page || 0],
    queryFn: () => apiClient.get(`${indexLink}?${createQueryString(queryParams)}`).then((response) => response.json()),
  });
  useEffect(() => {
    if (result.data) {
      result.data._embedded?.users.forEach((user: User) => queryClient.setQueryData(["user", user.name], user));
    }
  }, [queryClient, result.data]);
  return result;
};

export const useUser = (name: string): ApiResult<User> => {
  const indexLink = useRequiredIndexLink("users");
  return useQuery<User, Error>({
    queryKey: ["user", name],
    queryFn: () => apiClient.get(concat(indexLink, name)).then((response) => response.json()),
  });
};

export const useUserLink = (name: string, linkName: string): string | undefined => {
  const { data } = useUser(name);
  return (data?._links[linkName] as Link)?.href;
};

export const useUserPermissionOverview = (user: User): ApiResult<PermissionOverview> => {
  const overviewLink = user._links.permissionOverview as Link;
  return useQuery<PermissionOverview, Error>({
    queryKey: ["user", user.name, "permissionOverview"],
    queryFn: () => apiClient.get(overviewLink.href).then((response) => response.json()),
  });
};

const createUser = (link: string) => {
  return (user: UserCreation) => {
    return apiClient
      .post(link, user, "application/vnd.scmm-user+json;v=2")
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

export const useCreateUser = () => {
  const queryClient = useQueryClient();
  const link = useRequiredIndexLink("users");
  const { mutate, data, isPending, error } = useMutation<User, Error, UserCreation>({
    mutationFn: createUser(link),
    onSuccess: (user) => {
      queryClient.setQueryData(["user", user.name], user);
      return queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });
  return {
    create: (user: UserCreation) => mutate(user),
    isPending,
    error,
    user: data,
  };
};

export const useUpdateUser = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, User>({
    mutationFn: (user) => {
      const updateUrl = (user._links.update as Link).href;
      return apiClient.put(updateUrl, user, "application/vnd.scmm-user+json;v=2");
    },
    onSuccess: async (_, user) => {
      await queryClient.invalidateQueries({ queryKey: ["user", user.name] });
      await queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });
  return {
    update: (user: User) => mutate(user),
    isPending,
    error,
    isUpdated: !!data,
  };
};

export const useDeleteUser = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, User>({
    mutationFn: (user) => {
      const deleteUrl = (user._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    onSuccess: async (_, name) => {
      await queryClient.removeQueries({ queryKey: ["user", name] });
      await queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });
  return {
    remove: (user: User) => mutate(user),
    isPending,
    error,
    isDeleted: !!data,
  };
};

const convertToInternal = (url: string, newPassword: string) => {
  return apiClient.put(
    url,
    {
      newPassword,
    },
    "application/vnd.scmm-user+json;v=2",
  );
};

const convertToExternal = (url: string) => {
  return apiClient.put(url, {}, "application/vnd.scmm-user+json;v=2");
};

export type ConvertToInternalRequest = {
  user: User;
  password: string;
};

export const useConvertToInternal = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, ConvertToInternalRequest>({
    mutationFn: ({ user, password }) => convertToInternal((user._links.convertToInternal as Link).href, password),
    onSuccess: async (_, { user }) => {
      await queryClient.invalidateQueries({ queryKey: ["user", user.name] });
      await queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });
  return {
    convertToInternal: (user: User, password: string) => mutate({ user, password }),
    isPending,
    error,
    isConverted: !!data,
  };
};

export const useConvertToExternal = () => {
  const queryClient = useQueryClient();
  const { mutate, isPending, error, data } = useMutation<unknown, Error, User>({
    mutationFn: (user) => convertToExternal((user._links.convertToExternal as Link).href),
    onSuccess: async (_, user) => {
      await queryClient.invalidateQueries({ queryKey: ["user", user.name] });
      await queryClient.invalidateQueries({ queryKey: ["users"] });
    },
  });
  return {
    convertToExternal: (user: User) => mutate(user),
    isPending,
    error,
    isConverted: !!data,
  };
};

const CONTENT_TYPE_PASSWORD_OVERWRITE = "application/vnd.scmm-passwordOverwrite+json;v=2";

export const useSetUserPassword = (user: User) => {
  const { data, isPending, error, mutate, reset } = useMutation<unknown, Error, string>({
    mutationFn: (password) =>
      apiClient.put(
        requiredLink(user, "password"),
        {
          newPassword: password,
        },
        CONTENT_TYPE_PASSWORD_OVERWRITE,
      ),
  });
  return {
    setPassword: (newPassword: string) => mutate(newPassword),
    passwordOverwritten: !!data,
    isPending,
    error,
    reset,
  };
};

const CONTENT_TYPE_PASSWORD_CHANGE = "application/vnd.scmm-passwordChange+json;v=2";

type ChangeUserPasswordRequest = {
  oldPassword: string;
  newPassword: string;
};

export const useChangeUserPassword = (user: User | Me) => {
  const { data, isPending, error, mutate, reset } = useMutation<unknown, Error, ChangeUserPasswordRequest>({
    mutationFn: (request) => apiClient.put(requiredLink(user, "password"), request, CONTENT_TYPE_PASSWORD_CHANGE),
  });
  return {
    changePassword: (oldPassword: string, newPassword: string) => mutate({ oldPassword, newPassword }),
    passwordChanged: !!data,
    isPending,
    error,
    reset,
  };
};
