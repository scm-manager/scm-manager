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
 *
 */

import { ApiResult, useIndexJsonResource } from "./base";
import { useMutation, useQuery, useQueryClient } from "react-query";
import {
  Namespace,
  Permission,
  PermissionCollection,
  PermissionCreateEntry,
  Repository,
  RepositoryRoleCollection,
  RepositoryVerbs
} from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";
import { requiredLink } from "./links";
import { repoQueryKey } from "./keys";

export const useRepositoryRoles = (): ApiResult<RepositoryRoleCollection> => {
  return useIndexJsonResource<RepositoryRoleCollection>("repositoryRoles");
};

export const useRepositoryVerbs = (): ApiResult<RepositoryVerbs> => {
  return useIndexJsonResource<RepositoryVerbs>("repositoryVerbs");
};

export const useAvailablePermissions = () => {
  const roles = useRepositoryRoles();
  const verbs = useRepositoryVerbs();
  let data;
  if (roles.data && verbs.data) {
    data = {
      repositoryVerbs: verbs.data.verbs,
      repositoryRoles: roles.data._embedded.repositoryRoles
    };
  }

  return {
    isLoading: roles.isLoading || verbs.isLoading,
    error: roles.error || verbs.error,
    data
  };
};

const isRepository = (namespaceOrRepository: Namespace | Repository): namespaceOrRepository is Repository => {
  return (namespaceOrRepository as Repository).name !== undefined;
};

const createQueryKey = (namespaceOrRepository: Namespace | Repository) => {
  if (isRepository(namespaceOrRepository)) {
    return repoQueryKey(namespaceOrRepository, "permissions");
  } else {
    return ["namespace", namespaceOrRepository.namespace, "permissions"];
  }
};

export const usePermissions = (namespaceOrRepository: Namespace | Repository): ApiResult<PermissionCollection> => {
  const link = requiredLink(namespaceOrRepository, "permissions");
  const queryKey = createQueryKey(namespaceOrRepository);
  return useQuery<PermissionCollection, Error>(queryKey, () => apiClient.get(link).then(response => response.json()));
};

const createPermission = (link: string) => {
  return (permission: PermissionCreateEntry) => {
    return apiClient
      .post(link, permission, "application/vnd.scmm-repositoryPermission+json")
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

export const useCreatePermission = (namespaceOrRepository: Namespace | Repository) => {
  const queryClient = useQueryClient();
  const link = requiredLink(namespaceOrRepository, "permissions");
  const { isLoading, error, mutate, data } = useMutation<Permission, Error, PermissionCreateEntry>(
    createPermission(link),
    {
      onSuccess: () => {
        const queryKey = createQueryKey(namespaceOrRepository);
        return queryClient.invalidateQueries(queryKey);
      }
    }
  );
  return {
    isLoading,
    error,
    create: (permission: PermissionCreateEntry) => mutate(permission),
    permission: data
  };
};

export const useUpdatePermission = (namespaceOrRepository: Namespace | Repository) => {
  const queryClient = useQueryClient();
  const { isLoading, error, mutate, data } = useMutation<unknown, Error, Permission>(
    permission => {
      const link = requiredLink(permission, "update");
      return apiClient.put(link, permission, "application/vnd.scmm-repositoryPermission+json");
    },
    {
      onSuccess: () => {
        const queryKey = createQueryKey(namespaceOrRepository);
        return queryClient.invalidateQueries(queryKey);
      }
    }
  );
  return {
    isLoading,
    error,
    update: (permission: Permission) => mutate(permission),
    isUpdated: !!data
  };
};

export const useDeletePermission = (namespaceOrRepository: Namespace | Repository) => {
  const queryClient = useQueryClient();
  const { isLoading, error, mutate, data } = useMutation<unknown, Error, Permission>(
    permission => {
      const link = requiredLink(permission, "delete");
      return apiClient.delete(link);
    },
    {
      onSuccess: () => {
        const queryKey = createQueryKey(namespaceOrRepository);
        return queryClient.invalidateQueries(queryKey);
      }
    }
  );
  return {
    isLoading,
    error,
    remove: (permission: Permission) => mutate(permission),
    isDeleted: !!data
  };
};
