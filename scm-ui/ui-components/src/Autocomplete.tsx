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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSuggestions } from "@scm-manager/ui-api";
import { ComboboxField } from "@scm-manager/ui-core";
import { AutocompleteObject, Option } from "@scm-manager/ui-types";

export type SuggestionsType = "user" | "group";

type Props = {
  onSelect: (option: Option<AutocompleteObject>) => void;
  autocompleteLink: string;
  type: SuggestionsType;
  clearAfterSelect?: boolean;
};

/**
 * @beta
 * @since 4.0.0
 */
const Autocomplete: FC<Props> = ({ onSelect, autocompleteLink, type, clearAfterSelect }) => {
  const [t] = useTranslation("commons");
  const loadSuggestions = useSuggestions(autocompleteLink);
  const [selectedValue, setSelectedValue] = useState<Option<AutocompleteObject> | null>(null);

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

  const handleChange = (option: unknown) => {
    const typedOption = option as Option<AutocompleteObject> | null;

    if (typedOption) {
      onSelect(typedOption);
    }

    setSelectedValue(clearAfterSelect ? null : typedOption);
  };

  return (
    <ComboboxField
      label={t(`autocomplete.${type}.label`)}
      options={loadOptions}
      placeholder={t(`autocomplete.${type}.placeholder`)}
      onChange={handleChange}
      value={selectedValue}
    />
  );
};

export default Autocomplete;
