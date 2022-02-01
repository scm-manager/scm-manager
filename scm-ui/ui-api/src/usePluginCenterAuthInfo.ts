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
