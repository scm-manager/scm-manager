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
import { Link, Me, PermissionOverview, User, UserCollection, UserCreation } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import { createQueryString } from "./utils";
import { concat } from "./urls";
import { requiredLink } from "./links";

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

  return useQuery<UserCollection, Error>(
    ["users", request?.search || "", request?.page || 0],
    () => apiClient.get(`${indexLink}?${createQueryString(queryParams)}`).then((response) => response.json()),
    {
      onSuccess: (users: UserCollection) => {
        users._embedded?.users.forEach((user: User) => queryClient.setQueryData(["user", user.name], user));
      },
    }
  );
};

export const useUser = (name: string): ApiResult<User> => {
  const indexLink = useRequiredIndexLink("users");
  return useQuery<User, Error>(["user", name], () =>
    apiClient.get(concat(indexLink, name)).then((response) => response.json())
  );
};

export const useUserPermissionOverview = (user: User): ApiResult<PermissionOverview> => {
  const overviewLink = user._links.permissionOverview as Link;
  return useQuery<PermissionOverview, Error>(["user", user.name, "permissionOverview"], () =>
    apiClient.get(overviewLink.href).then((response) => response.json())
  );
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
  const { mutate, data, isLoading, error } = useMutation<User, Error, UserCreation>(createUser(link), {
    onSuccess: (user) => {
      queryClient.setQueryData(["user", user.name], user);
      return queryClient.invalidateQueries(["users"]);
    },
  });
  return {
    create: (user: UserCreation) => mutate(user),
    isLoading,
    error,
    user: data,
  };
};

export const useUpdateUser = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, User>(
    (user) => {
      const updateUrl = (user._links.update as Link).href;
      return apiClient.put(updateUrl, user, "application/vnd.scmm-user+json;v=2");
    },
    {
      onSuccess: async (_, user) => {
        await queryClient.invalidateQueries(["user", user.name]);
        await queryClient.invalidateQueries(["users"]);
      },
    }
  );
  return {
    update: (user: User) => mutate(user),
    isLoading,
    error,
    isUpdated: !!data,
  };
};

export const useDeleteUser = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, User>(
    (user) => {
      const deleteUrl = (user._links.delete as Link).href;
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: async (_, name) => {
        await queryClient.removeQueries(["user", name]);
        await queryClient.invalidateQueries(["users"]);
      },
    }
  );
  return {
    remove: (user: User) => mutate(user),
    isLoading,
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
    "application/vnd.scmm-user+json;v=2"
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
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, ConvertToInternalRequest>(
    ({ user, password }) => convertToInternal((user._links.convertToInternal as Link).href, password),
    {
      onSuccess: async (_, { user }) => {
        await queryClient.invalidateQueries(["user", user.name]);
        await queryClient.invalidateQueries(["users"]);
      },
    }
  );
  return {
    convertToInternal: (user: User, password: string) => mutate({ user, password }),
    isLoading,
    error,
    isConverted: !!data,
  };
};

export const useConvertToExternal = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, User>(
    (user) => convertToExternal((user._links.convertToExternal as Link).href),
    {
      onSuccess: async (_, user) => {
        await queryClient.invalidateQueries(["user", user.name]);
        await queryClient.invalidateQueries(["users"]);
      },
    }
  );
  return {
    convertToExternal: (user: User) => mutate(user),
    isLoading,
    error,
    isConverted: !!data,
  };
};

const CONTENT_TYPE_PASSWORD_OVERWRITE = "application/vnd.scmm-passwordOverwrite+json;v=2";

export const useSetUserPassword = (user: User) => {
  const { data, isLoading, error, mutate, reset } = useMutation<unknown, Error, string>((password) =>
    apiClient.put(
      requiredLink(user, "password"),
      {
        newPassword: password,
      },
      CONTENT_TYPE_PASSWORD_OVERWRITE
    )
  );
  return {
    setPassword: (newPassword: string) => mutate(newPassword),
    passwordOverwritten: !!data,
    isLoading,
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
  const { data, isLoading, error, mutate, reset } = useMutation<unknown, Error, ChangeUserPasswordRequest>((request) =>
    apiClient.put(requiredLink(user, "password"), request, CONTENT_TYPE_PASSWORD_CHANGE)
  );
  return {
    changePassword: (oldPassword: string, newPassword: string) => mutate({ oldPassword, newPassword }),
    passwordChanged: !!data,
    isLoading,
    error,
    reset,
  };
};
