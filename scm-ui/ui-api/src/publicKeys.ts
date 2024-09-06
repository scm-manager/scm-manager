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

import { Me, PublicKey, PublicKeyCreation, PublicKeysCollection, User } from "@scm-manager/ui-types";
import { ApiResult } from "./base";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { requiredLink } from "./links";

export const CONTENT_TYPE_PUBLIC_KEY = "application/vnd.scmm-publicKey+json;v=2";

export const usePublicKeys = (user: User | Me): ApiResult<PublicKeysCollection> =>
  useQuery(["user", user.name, "publicKeys"], () =>
    apiClient.get(requiredLink(user, "publicKeys")).then((r) => r.json())
  );

const createPublicKey =
  (link: string) =>
  async (key: PublicKeyCreation): Promise<PublicKey> => {
    const creationResponse = await apiClient.post(link, key, CONTENT_TYPE_PUBLIC_KEY);
    const location = creationResponse.headers.get("Location");
    if (!location) {
      throw new Error("Server does not return required Location header");
    }
    const apiKeyResponse = await apiClient.get(location);
    return apiKeyResponse.json();
  };

export const useCreatePublicKey = (user: User | Me, publicKeys: PublicKeysCollection) => {
  const queryClient = useQueryClient();
  const { mutate, data, isLoading, error, reset } = useMutation<PublicKey, Error, PublicKeyCreation>(
    createPublicKey(requiredLink(publicKeys, "create")),
    {
      onSuccess: () => queryClient.invalidateQueries(["user", user.name, "publicKeys"]),
    }
  );
  return {
    create: (key: PublicKeyCreation) => mutate(key),
    isLoading,
    error,
    apiKey: data,
    reset,
  };
};

export const useDeletePublicKey = (user: User | Me) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, PublicKey>(
    (publicKey) => {
      const deleteUrl = requiredLink(publicKey, "delete");
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: () => queryClient.invalidateQueries(["user", user.name, "publicKeys"]),
    }
  );
  return {
    remove: (publicKey: PublicKey) => mutate(publicKey),
    isLoading,
    error,
    isDeleted: !!data,
  };
};
