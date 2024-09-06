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

import { apiClient } from "./apiclient";
import { useQuery } from "react-query";
import { useIndexLinks } from "./base";
import { AutocompleteObject, Link, Option } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";

const defaultLabelFactory = (element: AutocompleteObject): string =>
  element.displayName ? `${element.displayName} (${element.id})` : element.id;

function useAutocompleteOptions(
  query = "",
  link?: string,
  options: {
    labelFactory?: (element: AutocompleteObject) => string;
    allowArbitraryValues?: (query: string) => AutocompleteObject;
  } = {}
) {
  const [t] = useTranslation("commons");
  return useQuery<Option<AutocompleteObject>[], Error>(
    ["options", link, query],
    () =>
      apiClient
        .get(link + "?q=" + query)
        .then((r) => r.json())
        .then((json: Array<AutocompleteObject>) => {
          const result: Array<Option<AutocompleteObject>> = json.map((element) => ({
            value: element,
            label: options.labelFactory ? options.labelFactory(element) : defaultLabelFactory(element),
          }));
          if (
            options.allowArbitraryValues &&
            !result.some(
              (opt) => opt.value.id === query || opt.value.displayName?.toLowerCase() === query.toLowerCase()
            )
          ) {
            result.unshift({
              value: options.allowArbitraryValues(query),
              label: query,
              displayValue: t("form.combobox.arbitraryDisplayValue", { query }),
            });
          }
          return result;
        }),
    {
      enabled: query.length > 1 && !!link,
    }
  );
}

export const useGroupOptions = (query?: string) => {
  const indexLinks = useIndexLinks();
  const autocompleteLink = (indexLinks.autocomplete as Link[]).find((i) => i.name === "groups");
  return useAutocompleteOptions(query, autocompleteLink?.href, {
    allowArbitraryValues: (query) => ({ id: query, displayName: query }),
  });
};

export const useNamespaceOptions = (query?: string) => {
  const indexLinks = useIndexLinks();
  const autocompleteLink = (indexLinks.autocomplete as Link[]).find((i) => i.name === "namespaces");
  return useAutocompleteOptions(query, autocompleteLink?.href, {
    allowArbitraryValues: (query) => ({ id: query, displayName: query }),
  });
};

export const useUserOptions = (query?: string) => {
  const indexLinks = useIndexLinks();
  const autocompleteLink = (indexLinks.autocomplete as Link[]).find((i) => i.name === "users");
  return useAutocompleteOptions(query, autocompleteLink?.href, {
    allowArbitraryValues: (query) => ({ id: query, displayName: query }),
  });
};
