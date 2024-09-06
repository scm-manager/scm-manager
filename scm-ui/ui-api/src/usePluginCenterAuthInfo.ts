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
import { Link, PluginCenterAuthenticationInfo } from "@scm-manager/ui-types";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiClient } from "./apiclient";
import { useLocation } from "react-router-dom";

const appendQueryParam = (link: Link, name: string, value: string) => {
  let href = link.href;
  if (href.includes("?")) {
    href += "&";
  } else {
    href += "?";
  }
  link.href = href + name + "=" + value;
};

export const usePluginCenterAuthInfo = (): ApiResult<PluginCenterAuthenticationInfo> => {
  const link = useIndexLink("pluginCenterAuth");
  const location = useLocation();
  return useQuery<PluginCenterAuthenticationInfo, Error>(
    ["pluginCenterAuth"],
    () => {
      if (!link) {
        throw new Error("no such plugin center auth link");
      }
      return apiClient
        .get(link)
        .then(response => response.json())
        .then((result: PluginCenterAuthenticationInfo) => {
          if (result._links?.login) {
            appendQueryParam(result._links.login as Link, "source", location.pathname);
          }
          if (result._links?.reconnect) {
            appendQueryParam(result._links.reconnect as Link, "source", location.pathname);
          }
          return result;
        });
    },
    {
      enabled: !!link
    }
  );
};

export const usePluginCenterLogout = (authenticationInfo: PluginCenterAuthenticationInfo) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error>(
    () => {
      if (!authenticationInfo._links.logout) {
        throw new Error("authenticationInfo has no logout link");
      }
      const logout = authenticationInfo._links.logout as Link;
      return apiClient.delete(logout.href);
    },
    {
      onSuccess: () => queryClient.invalidateQueries("pluginCenterAuth")
    }
  );

  return {
    logout: () => {
      mutate();
    },
    isLoading,
    error
  };
};
