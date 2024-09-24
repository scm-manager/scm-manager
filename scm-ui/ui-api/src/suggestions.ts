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

import { AutocompleteObject, SelectValue } from "@scm-manager/ui-types";
import { apiClient } from "./apiclient";

export const useSuggestions: (link?: string) => (query: string) => Promise<SelectValue[]> = (link) => {
  if (!link) {
    return () => Promise.resolve([]);
  }
  const url = link + "?q=";
  return (inputValue) => {
    // Prevent violate input condition of api call because parameter length is too short
    if (inputValue.length < 2) {
      return Promise.resolve([]);
    }
    return apiClient
      .get(url + inputValue)
      .then((response) => response.json())
      .then((json: AutocompleteObject[]) =>
        json.map((element) => ({
          value: element,
          label: element.displayName ? `${element.displayName} (${element.id})` : element.id,
        }))
      );
  };
};
