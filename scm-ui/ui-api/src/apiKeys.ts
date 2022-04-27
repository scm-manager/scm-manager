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
import { ApiKey, ApiKeyCreation, ApiKeysCollection, ApiKeyWithToken, Me, User } from "@scm-manager/ui-types";
import { ApiResult } from "./base";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { requiredLink } from "./links";

const CONTENT_TYPE_API_KEY = "application/vnd.scmm-apiKey+json;v=2";

export const useApiKeys = (user: User | Me): ApiResult<ApiKeysCollection> =>
  useQuery(["user", user.name, "apiKeys"], () => apiClient.get(requiredLink(user, "apiKeys")).then((r) => r.json()));

const createApiKey =
  (link: string) =>
  async (key: ApiKeyCreation): Promise<ApiKeyWithToken> => {
    const creationResponse = await apiClient.post(link, key, CONTENT_TYPE_API_KEY);
    const location = creationResponse.headers.get("Location");
    if (!location) {
      throw new Error("Server does not return required Location header");
    }
    const locationResponse = await apiClient.get(location);
    const [apiKey, token] = await Promise.all([locationResponse.json(), creationResponse.text()]);
    return { ...apiKey, token } as ApiKeyWithToken;
  };

export const useCreateApiKey = (user: User | Me, apiKeys: ApiKeysCollection) => {
  const queryClient = useQueryClient();
  const { mutate, data, isLoading, error, reset } = useMutation<ApiKeyWithToken, Error, ApiKeyCreation>(
    createApiKey(requiredLink(apiKeys, "create")),
    {
      onSuccess: () => queryClient.invalidateQueries(["user", user.name, "apiKeys"]),
    }
  );
  return {
    create: (key: ApiKeyCreation) => mutate(key),
    isLoading,
    error,
    apiKey: data,
    reset,
  };
};

export const useDeleteApiKey = (user: User | Me) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error, data } = useMutation<unknown, Error, ApiKey>(
    (apiKey) => {
      const deleteUrl = requiredLink(apiKey, "delete");
      return apiClient.delete(deleteUrl);
    },
    {
      onSuccess: () => queryClient.invalidateQueries(["user", user.name, "apiKeys"]),
    }
  );
  return {
    remove: (apiKey: ApiKey) => mutate(apiKey),
    isLoading,
    error,
    isDeleted: !!data,
  };
};
