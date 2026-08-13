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

import { Link, Me } from "@scm-manager/ui-types";
import { useMutation, useQuery } from "@tanstack/react-query";
import { apiClient } from "./apiclient";
import { ApiResult, useIndexLink } from "./base";
import { useReset } from "./reset";
import { useCallback } from "react";

export const useMe = (): ApiResult<Me> => {
  const link = useIndexLink("me");
  return useQuery<Me, Error>({
    queryKey: ["me"],
    queryFn: () => apiClient.get(link!).then((response) => response.json()),
    enabled: !!link,
  });
};

export const useRequiredMe = () => {
  const { data } = useMe();
  if (!data) {
    throw new Error("Could not find 'me' in cache");
  }
  return data;
};

export const useMeLink = (linkName: string): string | undefined => {
  const { data } = useMe();
  return (data?._links[linkName] as Link)?.href;
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

export const useLogin = (link: string, onSuccess: () => void) => {
  const reset = useReset();
  const { mutate, isPending, isSuccess, error } = useMutation<unknown, Error, Credentials>({
    mutationFn: (credentials) => apiClient.post(link!, credentials),
    onSuccess: () => {
      reset();
      onSuccess();
    },
  });
  if (!link) {
    return {
      login: undefined,
      isPending: false,
      isSuccess: false,
      error: undefined,
    };
  }

  const login = (username: string, password: string) => {
    mutate({ cookie: true, grant_type: "password", username, password });
  };

  return {
    login: link ? login : undefined,
    isPending,
    isSuccess,
    error,
  };
};

type LogoutResponse = {
  logoutRedirect?: string;
};

export const useLogout = (link: string) => {
  const reset = useReset();

  const { mutate, isPending, error, data } = useMutation<LogoutResponse, Error, unknown>({
    mutationFn: async () => {
      return apiClient.delete(link!).then((r) => (r.status === 200 ? r.json() : {}));
    },
    onSuccess: (response) => {
      if (response?.logoutRedirect) {
        window.location.assign(response.logoutRedirect);
      }
      return reset();
    },
  });

  const logout = useCallback(() => {
    mutate({});
  }, [mutate]);

  return {
    logout: link && !data ? logout : undefined,
    isPending,
    error,
  };
};
