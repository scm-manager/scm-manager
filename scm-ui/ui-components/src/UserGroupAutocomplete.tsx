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
import { SelectValue } from "@scm-manager/ui-types";
import Autocomplete from "./Autocomplete";
import { useSuggestions } from "@scm-manager/ui-api";

export type AutocompleteProps = {
  autocompleteLink?: string;
  valueSelected?: (p: SelectValue) => void;
  value?: SelectValue;
};

type Props = AutocompleteProps & {
  label: string;
  noOptionsMessage: string;
  loadingMessage: string;
  placeholder: string;
};

/**
 * @deprecated
 * @since 2.45.0
 *
 * Use {@link Combobox} instead
 */
const UserGroupAutocomplete: FC<Props> = ({ autocompleteLink, valueSelected, ...props }) => {
  const loadSuggestions = useSuggestions(autocompleteLink);

  const selectName = (selection: SelectValue) => {
    if (valueSelected) {
      valueSelected(selection);
    }
  };

  return <Autocomplete loadSuggestions={loadSuggestions} valueSelected={selectName} creatable={true} {...props} />;
};

export default UserGroupAutocomplete;
