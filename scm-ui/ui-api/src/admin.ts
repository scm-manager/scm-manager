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

import { ApiResult, useIndexLink } from "./base";
import { UpdateInfo } from "@scm-manager/ui-types";
import { useQuery } from "react-query";
import { apiClient } from "./apiclient";

export const useUpdateInfo = (): ApiResult<UpdateInfo | null> => {
  const indexLink = useIndexLink("updateInfo");
  return useQuery<UpdateInfo | null, Error>(
    "updateInfo",
    () => {
      if (!indexLink) {
        throw new Error("could not find index data");
      }
      return apiClient.get(indexLink).then((response) => (response.status === 204 ? null : response.json()));
    },
    { enabled: !!indexLink }
  );
};
