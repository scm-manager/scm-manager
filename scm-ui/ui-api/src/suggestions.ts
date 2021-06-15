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
