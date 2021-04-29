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

import { useMe } from "./login";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { Link, NotificationCollection } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";
import { useCallback } from "react";
import { requiredLink } from "./links";

export const useNotifications = () => {
  const { data: me } = useMe();
  const link = (me?._links["notifications"] as Link)?.href;
  const { data, error, isLoading, refetch } = useQuery<NotificationCollection, Error>(
    "notifications",
    () => apiClient.get(link).then(response => response.json()),
    {
      enabled: !!link
    }
  );

  const memoizedRefetch = useCallback(() => {
    return refetch().then(r => r.data);
  }, [refetch]);

  return {
    data,
    error,
    isLoading,
    refetch: memoizedRefetch
  };
};

export const useClearNotifications = (notificationCollection: NotificationCollection) => {
  const queryClient = useQueryClient();
  const link = requiredLink(notificationCollection, "clear");
  const { data, isLoading, error, mutate } = useMutation<Response, Error>(() => apiClient.delete(link), {
    onSuccess: () => {
      queryClient.invalidateQueries("notifications");
    }
  });
  return {
    isLoading,
    error,
    clear: () => mutate(),
    isCleared: !!data
  };
};
