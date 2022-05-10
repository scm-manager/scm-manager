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

import { Me } from "@scm-manager/ui-types";
import { useMutation, useQuery } from "react-query";
import { apiClient } from "./apiclient";
import { ApiResult, useIndexLink } from "./base";
import { useLegacyContext } from "./LegacyContext";
import { useReset } from "./reset";
import { useCallback } from "react";

export const useMe = (): ApiResult<Me> => {
  const legacy = useLegacyContext();
  const link = useIndexLink("me");
  return useQuery<Me, Error>("me", () => apiClient.get(link!).then((response) => response.json()), {
    enabled: !!link,
    onSuccess: (me) => {
      if (legacy.onMeFetched) {
        legacy.onMeFetched(me);
      }
    },
  });
};

export const useRequiredMe = () => {
  const { data } = useMe();
  if (!data) {
    throw new Error("Could not find 'me' in cache");
  }
  return data;
};

export const useSubject = () => {
  const link = useIndexLink("login");
  const { isLoading, error, data: me } = useMe();
  const isAnonymous = me?.name === "_anonymous";
  const isAuthenticated = !isAnonymous && !!me && !link;
  return {
    isAuthenticated,
    isAnonymous,
    isLoading,
    error,
    me,
  };
};

type Credentials = {
  username: string;
  password: string;
  cookie: boolean;
  grant_type: string;
};

export const useLogin = () => {
  const link = useIndexLink("login");
  const reset = useReset();
  const { mutate, isLoading, error } = useMutation<unknown, Error, Credentials>(
    (credentials) => apiClient.post(link!, credentials),
    {
      onSuccess: reset,
    }
  );

  const login = (username: string, password: string) => {
    // grant_type is specified by the oauth standard with the underscore
    // so we stick with it, even if eslint does not like it.
    // eslint-disable-next-line @typescript-eslint/camelcase
    mutate({ cookie: true, grant_type: "password", username, password });
  };

  return {
    login: link ? login : undefined,
    isLoading,
    error,
  };
};

type LogoutResponse = {
  logoutRedirect?: string;
};

export const useLogout = () => {
  const link = useIndexLink("logout");
  const reset = useReset();

  const { mutate, isLoading, error, data } = useMutation<LogoutResponse, Error, unknown>(
    () => apiClient.delete(link!).then((r) => (r.status === 200 ? r.json() : {})),
    {
      onSuccess: (response) => {
        if (response?.logoutRedirect) {
          window.location.assign(response.logoutRedirect);
        }
        return reset();
      },
    }
  );

  const logout = useCallback(() => {
    mutate({});
  }, [mutate]);

  return {
    logout: link && !data ? logout : undefined,
    isLoading,
    error,
  };
};
