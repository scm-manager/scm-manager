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

import { useQuery } from "react-query";
import { apiClient } from "./apiclient";
import { ApiResult, useIndexLink } from "./base";
import { AlertsResponse, HalRepresentation, Link } from "@scm-manager/ui-types";

type AlertRequest = HalRepresentation & {
  checksum: string;
  body: unknown;
};

type LocalStorageAlerts = AlertsResponse & {
  checksum: string;
};

const alertsFromStorage = (): LocalStorageAlerts | undefined => {
  const item = localStorage.getItem("alerts");
  if (item) {
    return JSON.parse(item);
  }
};

const fetchAlerts = (request: AlertRequest) => {
  const url = (request._links["alerts"] as Link)?.href;
  if (!url) {
    throw new Error("no alerts link defined");
  }
  return fetch(url, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(request.body)
  })
    .then(response => {
      if (!response.ok) {
        throw new Error("Failed to fetch alerts");
      }
      return response;
    })
    .then(response => response.json())
    .then((data: AlertsResponse) => {
      const storageItem: LocalStorageAlerts = {
        ...data,
        checksum: request.checksum
      };
      localStorage.setItem("alerts", JSON.stringify(storageItem));
      return data;
    });
};

const restoreOrFetch = (request: AlertRequest): Promise<AlertsResponse> => {
  const storedAlerts = alertsFromStorage();
  if (!storedAlerts || storedAlerts.checksum !== request.checksum) {
    return fetchAlerts(request);
  }
  return Promise.resolve(storedAlerts);
};

export const useAlerts = (): ApiResult<AlertsResponse> => {
  const link = useIndexLink("alerts");
  const { data, error, isLoading } = useQuery<AlertsResponse, Error>(
    "alerts",
    () => {
      if (!link) {
        throw new Error("Could not find alert link");
      }
      return apiClient
        .get(link)
        .then(response => response.json())
        .then(restoreOrFetch);
    },
    {
      enabled: !!link,
      staleTime: Infinity
    }
  );

  return {
    data,
    error,
    isLoading
  };
};
