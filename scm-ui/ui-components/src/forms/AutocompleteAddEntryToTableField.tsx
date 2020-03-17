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
import React, { MouseEvent } from "react";
import styled from "styled-components";
import { SelectValue } from "@scm-manager/ui-types";
import Level from "../layout/Level";
import Autocomplete from "../Autocomplete";
import AddButton from "../buttons/AddButton";

type Props = {
  addEntry: (p: SelectValue) => void;
  disabled: boolean;
  buttonLabel: string;
  fieldLabel: string;
  helpText?: string;
  loadSuggestions: (p: string) => Promise<SelectValue[]>;
  placeholder?: string;
  loadingMessage?: string;
  noOptionsMessage?: string;
};

type State = {
  selectedValue?: SelectValue;
};

const StyledAutocomplete = styled(Autocomplete)`
  width: 100%;
  margin-right: 1.5rem;
`;

class AutocompleteAddEntryToTableField extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      selectedValue: undefined
    };
  }
  render() {
    const {
      disabled,
      buttonLabel,
      fieldLabel,
      helpText,
      loadSuggestions,
      placeholder,
      loadingMessage,
      noOptionsMessage
    } = this.props;

    const { selectedValue } = this.state;
    return (
      <Level
        children={
          <StyledAutocomplete
            label={fieldLabel}
            loadSuggestions={loadSuggestions}
            valueSelected={this.handleAddEntryChange}
            helpText={helpText}
            value={selectedValue}
            placeholder={placeholder}
            loadingMessage={loadingMessage}
            noOptionsMessage={noOptionsMessage}
            creatable={true}
          />
        }
        right={
          <div className="field">
            <AddButton label={buttonLabel} action={this.addButtonClicked} disabled={disabled} />
          </div>
        }
      />
    );
  }

  addButtonClicked = (event: MouseEvent) => {
    event.preventDefault();
    this.appendEntry();
  };

  appendEntry = () => {
    const { selectedValue } = this.state;
    if (!selectedValue) {
      return;
    }
    this.setState(
      {
        ...this.state,
        // @ts-ignore null is needed to clear the selection; undefined does not work
        selectedValue: null
      },
      () => this.props.addEntry(selectedValue)
    );
  };

  handleAddEntryChange = (selection: SelectValue) => {
    this.setState({
      ...this.state,
      selectedValue: selection
    });
  };
}

export default AutocompleteAddEntryToTableField;
