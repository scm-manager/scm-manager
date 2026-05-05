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

import { HalRepresentation, IndexResources, Link } from "@scm-manager/ui-types";
import { useQuery, useSuspenseQuery } from "@tanstack/react-query";
import { apiClient } from "./apiclient";
import { MissingLinkError, UnauthorizedError } from "./errors";
import { requiredLink } from "./links";
import { queryClient } from "./ApiProvider";

export type ApiResult<T> = {
  isLoading: boolean;
  error: Error | null;
  data?: T;
};

export type ApiResultWithFetching<T> = ApiResult<T> & {
  isFetching: boolean;
};

export type DeleteFunction<T> = (entity: T) => void;

const fetchIndex = () => apiClient.get("/").then((response) => response.json() as Promise<IndexResources>);

export const indexLoader = async () => {
  const queryData = queryClient.getQueryData(["index"]);
  if (queryData) {
    return queryData;
  }
  return await queryClient.fetchQuery({
    queryKey: ["index"],
    queryFn: fetchIndex,
    retry: (failureCount, error) => {
      return error instanceof UnauthorizedError && failureCount === 0;
    },
  });
};

export const useIndex = (): IndexResources => {
  const { data: index } = useSuspenseQuery<IndexResources, Error>({
    queryKey: ["index"],
    queryFn: fetchIndex,
    retry: (failureCount, error) => {
      // The index resource returns a 401 if the access token expired.
      // This only happens once because the error response automatically invalidates the cookie.
      // In this event, we have to try the request once again.
      return error instanceof UnauthorizedError && failureCount === 0;
    },
  });
  return index!;
};

export const useIndexLink = (name: string): string | undefined => {
  const data = useIndex();
  return (data?._links[name] as Link)?.href;
};

export const useIndexLinks = () => {
  return useIndex()._links;
};

export const useRequiredIndexLink = (name: string): string => {
  const link = useIndexLink(name);
  if (!link) {
    throw new MissingLinkError(`Could not find link ${name} in index resource`);
  }
  return link;
};

export const useVersion = (): string => {
  const data = useIndex();
  const { version } = data;
  if (!version) {
    throw new Error("could not find version in index data");
  }
  return version;
};

export const useIndexJsonResource = <T>(name: string): ApiResult<T> => {
  const link = useIndexLink(name);
  return useQuery<T, Error>({
    queryKey: [name],
    queryFn: () => apiClient.get(link!).then((response) => response.json()),
    enabled: !!link,
  });
};

export const useJsonResource = <T>(entity: HalRepresentation, name: string, key: string[]): ApiResult<T> =>
  useQuery<T, Error>({
    queryKey: key,
    queryFn: () => apiClient.get(requiredLink(entity, name)).then((response) => response.json()),
  });

export function fetchResourceFromLocationHeader(response: Response) {
  const location = response.headers.get("Location");
  if (!location) {
    throw new Error("Server does not return required Location header");
  }
  return apiClient.get(location);
}

export const getResponseJson = (response: Response) => response.json();
