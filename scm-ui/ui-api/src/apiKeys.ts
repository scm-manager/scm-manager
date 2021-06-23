import { ApiKey, ApiKeyCreation, ApiKeysCollection, ApiKeyWithToken, Me, User } from "@scm-manager/ui-types";
import { RefetchableApiResult } from "./base";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { requiredLink } from "./links";

const CONTENT_TYPE_API_KEY = "application/vnd.scmm-apiKey+json;v=2";

export const useApiKeys = (user: User | Me): RefetchableApiResult<ApiKeysCollection> =>
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
    const [apiKey, token] = await Promise.all<ApiKey, string>([locationResponse.json(), creationResponse.text()]);
    return { ...apiKey, token } as ApiKeyWithToken;
  };

export const useCreateApiKey = (user: User | Me, apiKeys: ApiKeysCollection) => {
  const queryClient = useQueryClient();
  const { mutate, data, isLoading, error, reset } = useMutation<ApiKeyWithToken, Error, ApiKeyCreation>(
    createApiKey(requiredLink(apiKeys, "create")),
    {
      onSuccess: (apiKey) => {
        queryClient.setQueryData(["user", user.name, "apiKey", apiKey.id], apiKey);
        return queryClient.invalidateQueries(["user", user.name, "apiKeys"]);
      },
    }
  );
  return {
    create: (key: ApiKeyCreation) => mutate(key),
    isLoading,
    error,
    apiKey: data,
    reset
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
      onSuccess: async (_, apiKey) => {
        await queryClient.invalidateQueries(["user", user.name, "apiKey", apiKey.id]);
        await queryClient.invalidateQueries(["user", user.name, "apiKeys"]);
      },
    }
  );
  return {
    remove: (apiKey: ApiKey) => mutate(apiKey),
    isLoading,
    error,
    isDeleted: !!data,
  };
};
