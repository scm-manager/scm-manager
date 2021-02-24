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
import React from "react";
import { SelectValue, AutocompleteObject } from "@scm-manager/ui-types";
import Autocomplete from "./Autocomplete";
import { apiClient } from "@scm-manager/ui-api";

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

export default class UserGroupAutocomplete extends React.Component<Props> {
  loadSuggestions = (inputValue: string): Promise<SelectValue[]> => {
    const url = this.props.autocompleteLink;
    const link = url + "?q=";
    return apiClient
      .get(link + inputValue)
      .then(response => response.json())
      .then((json: AutocompleteObject[]) => {
        return json.map(element => {
          const label = element.displayName ? `${element.displayName} (${element.id})` : element.id;
          return {
            value: element,
            label
          };
        });
      });
  };

  selectName = (selection: SelectValue) => {
    if (this.props.valueSelected) {
      this.props.valueSelected(selection);
    }
  };

  render() {
    return (
      <Autocomplete
        loadSuggestions={this.loadSuggestions}
        valueSelected={this.selectName}
        creatable={true}
        {...this.props}
      />
    );
  }
}
