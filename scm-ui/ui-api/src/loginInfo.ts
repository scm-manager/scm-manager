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
import { useQuery } from "react-query";
import { LoginInfo } from "@scm-manager/ui-types";

export const useLoginInfo = (disabled = false): ApiResult<LoginInfo> => {
  const loginInfoLink = useIndexLink("loginInfo");
  const { error, isLoading, data } = useQuery<LoginInfo, Error>(
    ["loginInfo"],
    () => fetch(loginInfoLink!).then(response => response.json()),
    {
      enabled: !disabled && !!loginInfoLink,
      refetchOnWindowFocus: false
    }
  );
  return {
    data,
    error,
    isLoading
  };
};
