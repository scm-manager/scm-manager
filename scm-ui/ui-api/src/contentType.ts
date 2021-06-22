import { apiClient } from "./apiclient";
import { useQuery } from "react-query";
import { ApiResult } from "./base";

export type ContentType = {
  type: string;
  language?: string;
};

function getContentType(url: string): Promise<ContentType> {
  return apiClient.head(url).then((response) => {
    return {
      type: response.headers.get("Content-Type") || "application/octet-stream",
      language: response.headers.get("X-Programming-Language") || undefined,
    };
  });
}

export const useContentType = (url: string): ApiResult<ContentType> => {
  const { isLoading, error, data } = useQuery<ContentType, Error>(["contentType", url], () => getContentType(url));
  return {
    isLoading,
    error,
    data,
  };
};
