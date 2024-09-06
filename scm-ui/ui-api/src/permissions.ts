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

import { ApiResult, useIndexJsonResource, useJsonResource } from "./base";
import { useMutation, useQuery, useQueryClient } from "react-query";
import {
  GlobalPermissionsCollection,
  Group,
  Namespace,
  Permission,
  PermissionCollection,
  PermissionCreateEntry,
  Repository,
  RepositoryVerbs,
  User,
} from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import { objectLink, requiredLink } from "./links";
import { repoQueryKey } from "./keys";
import { useRepositoryRoles } from "./repository-roles";

export const useRepositoryVerbs = (): ApiResult<RepositoryVerbs> => {
  return useIndexJsonResource<RepositoryVerbs>("repositoryVerbs");
};

/**
 * *IMPORTANT NOTE:* These are actually *REPOSITORY* permissions.
 */
export const useAvailablePermissions = () => {
  const roles = useRepositoryRoles();
  const verbs = useRepositoryVerbs();
  let data;
  if (roles.data && verbs.data) {
    data = {
      repositoryVerbs: verbs.data.verbs,
      repositoryRoles: roles.data._embedded?.repositoryRoles || [],
    };
  }

  return {
    isLoading: roles.isLoading || verbs.isLoading,
    error: roles.error || verbs.error,
    data,
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
  return useQuery<PermissionCollection, Error>(queryKey, () => apiClient.get(link).then((response) => response.json()));
};

const createPermission = (link: string) => {
  return (permission: PermissionCreateEntry) => {
    return apiClient
      .post(link, permission, "application/vnd.scmm-repositoryPermission+json")
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

export const useCreatePermission = (namespaceOrRepository: Namespace | Repository) => {
  const queryClient = useQueryClient();
  const link = requiredLink(namespaceOrRepository, "permissions");
  const { isLoading, error, mutate, data } = useMutation<Permission, Error, PermissionCreateEntry>(
    createPermission(link),
    {
      onSuccess: () => {
        const queryKey = createQueryKey(namespaceOrRepository);
        return queryClient.invalidateQueries(queryKey);
      },
    }
  );
  return {
    isLoading,
    error,
    create: (permission: PermissionCreateEntry) => mutate(permission),
    permission: data,
  };
};

export const useUpdatePermission = (namespaceOrRepository: Namespace | Repository) => {
  const queryClient = useQueryClient();
  const { isLoading, error, mutate, data } = useMutation<unknown, Error, Permission>(
    (permission) => {
      const link = requiredLink(permission, "update");
      return apiClient.put(link, permission, "application/vnd.scmm-repositoryPermission+json");
    },
    {
      onSuccess: () => {
        const queryKey = createQueryKey(namespaceOrRepository);
        return queryClient.invalidateQueries(queryKey);
      },
    }
  );
  return {
    isLoading,
    error,
    update: (permission: Permission) => mutate(permission),
    isUpdated: !!data,
  };
};

export const useDeletePermission = (namespaceOrRepository: Namespace | Repository) => {
  const queryClient = useQueryClient();
  const { isLoading, error, mutate, data } = useMutation<unknown, Error, Permission>(
    (permission) => {
      const link = requiredLink(permission, "delete");
      return apiClient.delete(link);
    },
    {
      onSuccess: () => {
        const queryKey = createQueryKey(namespaceOrRepository);
        return queryClient.invalidateQueries(queryKey);
      },
    }
  );
  return {
    isLoading,
    error,
    remove: (permission: Permission) => mutate(permission),
    isDeleted: !!data,
  };
};

const userPermissionsKey = (user: User) => ["user", user.name, "permissions"];
const groupPermissionsKey = (group: Group) => ["group", group.name, "permissions"];

export const useGroupPermissions = (group: Group) =>
  useJsonResource<GlobalPermissionsCollection>(group, "permissions", groupPermissionsKey(group));
export const useUserPermissions = (user: User) =>
  useJsonResource<GlobalPermissionsCollection>(user, "permissions", userPermissionsKey(user));
export const useAvailableGlobalPermissions = () =>
  useIndexJsonResource<Omit<GlobalPermissionsCollection, "_links">>("permissions");

const useSetEntityPermissions = (permissionCollection?: GlobalPermissionsCollection, key?: string[]) => {
  const queryClient = useQueryClient();
  const url = permissionCollection ? objectLink(permissionCollection, "overwrite") : null;
  const { isLoading, error, mutate, data } = useMutation<unknown, Error, string[]>(
    (permissions) =>
      apiClient.put(
        url!,
        {
          permissions,
        },
        "application/vnd.scmm-permissionCollection+json;v=2"
      ),
    {
      onSuccess: () => queryClient.invalidateQueries(key),
    }
  );
  const setPermissions = (permissions: string[]) => mutate(permissions);
  return {
    isLoading,
    error,
    setPermissions: url ? setPermissions : undefined,
    isUpdated: !!data,
  };
};

export const useSetUserPermissions = (user: User, permissions?: GlobalPermissionsCollection) =>
  useSetEntityPermissions(permissions, userPermissionsKey(user));
export const useSetGroupPermissions = (group: Group, permissions?: GlobalPermissionsCollection) =>
  useSetEntityPermissions(permissions, groupPermissionsKey(group));
