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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { useSuggestions } from "@scm-manager/ui-api";
import { ComboboxField } from "@scm-manager/ui-core";
import { AutocompleteObject, Option } from "@scm-manager/ui-types";

export type SuggestionsType = "user" | "group";

type Props = {
  onSelect: (option: Option<AutocompleteObject>) => void;
  autocompleteLink: string;
  type: SuggestionsType;
};

const Autocomplete: FC<Props> = ({ onSelect, autocompleteLink, type }) => {
  const [t] = useTranslation("plugins");
  const loadSuggestions = useSuggestions(autocompleteLink);

  const loadOptions = async (query: string): Promise<Option<AutocompleteObject>[]> => {
    const suggestions = await loadSuggestions(query);

    if (query && query.trim() !== "") {
      const customOption: Option<AutocompleteObject> = {
        value: { id: query, displayName: query },
        label: query,
      };

      const exactMatch = suggestions.find((s) => s.label === query);
      return exactMatch ? suggestions : [customOption, ...suggestions];
    }

    return suggestions;
  };

  return (
    <ComboboxField
      label={t(`scm-review-plugin.config.branchProtection.bypasses.add.${type}.label`)}
      options={loadOptions}
      placeholder={t(`scm-review-plugin.config.branchProtection.bypasses.add.${type}.placeholder`)}
      onChange={(option) => {
        if (option) {
          onSelect(option as Option<AutocompleteObject>);
        }
      }}
      value={null}
    />
  );
};

export default Autocomplete;
