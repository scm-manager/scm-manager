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

import { useQuery } from "react-query";
import { apiClient } from "./apiclient";
import { ApiResult, useRequiredIndexLink } from "./base";
import { AlertsResponse } from "@scm-manager/ui-types";

type AlertRequest = {
  url: string;
  body: unknown;
};

export const useAlerts = (): ApiResult<AlertsResponse> => {
  const link = useRequiredIndexLink("alerts");
  const { data, error, isLoading } = useQuery<AlertsResponse, Error>("alerts", () =>
    apiClient
      .get(link)
      .then(response => response.json())
      .then((json: AlertRequest) =>
        fetch(json.url, {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(json.body)
        })
          .then(response => {
            if (!response.ok) {
              throw new Error("Failed to fetch alerts");
            }
            return response;
          })
          .then(response => response.json())
      )
  );

  return {
    data,
    error,
    isLoading
  };
};
