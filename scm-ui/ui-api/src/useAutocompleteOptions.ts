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
