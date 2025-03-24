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

import { requiredLink } from "./links";
import { Changeset } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import { useMutation } from "react-query";

export type RevertRequest = {
  branch: string;
  message: string;
};

export type RevertResponse = {
  revision: string;
};

export const useRevert = (changeset: Changeset) => {
  const link = requiredLink(changeset, "revert");
  const { isLoading, error, mutate, data } = useMutation<RevertResponse, Error, RevertRequest>({
    mutationFn: async (request: RevertRequest) => {
      const response = await apiClient.post(link, request, "application/vnd.scmm-revert+json;v=2");
      return await response.json();
    },
  });
  return {
    revert: mutate,
    isLoading,
    error,
    revision: data,
  };
};
