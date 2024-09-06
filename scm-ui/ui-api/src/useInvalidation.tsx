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

import { useMutation } from "react-query";
import { apiClient } from "./apiclient";
import { useRequiredIndexLink } from "./base";

const useInvalidation = (link: string) => {
  const { mutate, isLoading, error, isSuccess } = useMutation<unknown, Error, string>((link) =>
    apiClient.post(link, {})
  );

  return {
    invalidate: () => mutate(link),
    isLoading,
    isSuccess,
    error,
  };
};

export const useInvalidateAllCaches = () => {
  const invalidateCacheLink = useRequiredIndexLink("invalidateCaches");
  return useInvalidation(invalidateCacheLink);
};

export const useInvalidateSearchIndices = () => {
  const invalidateSearchIndexLink = useRequiredIndexLink("invalidateSearchIndex");
  return useInvalidation(invalidateSearchIndexLink);
};
