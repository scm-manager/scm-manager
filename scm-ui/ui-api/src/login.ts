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
import { useQuery, useMutation, useQueryClient } from "react-query";
import { apiClient } from "@scm-manager/ui-components";
import { ApiResult, useIndexLink, useRequiredIndexLink } from "./base";
import { useLegacyContext } from "./LegacyContext";

export const useMe = (): ApiResult<Me> => {
  const legacy = useLegacyContext();
  const link = useIndexLink("me");
  return useQuery<Me, Error>("me", () => apiClient.get(link!).then(response => response.json()), {
    enabled: !!link,
    onSuccess: me => {
      if (legacy.onMeFetched) {
        legacy.onMeFetched(me);
      }
    }
  });
};

export const useSubject = () => {
  const link = useIndexLink("login");
  const { isLoading, error, data: me } = useMe();
  const isAnonymous = me?.name === "_anonymous";
  const isAuthenticated = (me && !link) || isAnonymous;
  return {
    isAuthenticated,
    isAnonymous,
    isLoading,
    error,
    me
  };
};

type Credentials = {
  username: string;
  password: string;
  cookie: boolean;
  grant_type: string;
};

export const useLogin = () => {
  const link = useRequiredIndexLink("login");
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error, Credentials>(
    credentials => apiClient.post(link, credentials),
    {
      onSuccess: () => {
        return queryClient.invalidateQueries();
      }
    }
  );

  return {
    login: (username: string, password: string) => {
      mutate({ cookie: true, grant_type: "password", username, password });
    },
    isLoading,
    error
  };
};
