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
import React, { ChangeEvent } from "react";
import classNames from "classnames";
import LabelWithHelpIcon from "./LabelWithHelpIcon";
import {createAttributesForTesting} from "../devBuild";

export type SelectItem = {
  value: string;
  label: string;
};

type Props = {
  name?: string;
  label?: string;
  options: SelectItem[];
  value?: string;
  onChange: (value: string, name?: string) => void;
  loading?: boolean;
  helpText?: string;
  disabled?: boolean;
  testId?: string;
};

class Select extends React.Component<Props> {
  field: HTMLSelectElement | null | undefined;

  componentDidMount() {
    // trigger change after render, if value is null to set it to the first value
    // of the given options.
    if (!this.props.value && this.field && this.field.value) {
      this.props.onChange(this.field.value);
    }
  }

  handleInput = (event: ChangeEvent<HTMLSelectElement>) => {
    this.props.onChange(event.target.value, this.props.name);
  };

  render() {
    const { options, value, label, helpText, loading, disabled, testId } = this.props;
    const loadingClass = loading ? "is-loading" : "";

    return (
      <div className="field">
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className={classNames("control select", loadingClass)}>
          <select
            ref={input => {
              this.field = input;
            }}
            value={value}
            onChange={this.handleInput}
            disabled={disabled}
            {...createAttributesForTesting(testId)}
          >
            {options.map(opt => {
              return (
                <option value={opt.value} key={"KEY_" + opt.value}>
                  {opt.label}
                </option>
              );
            })}
          </select>
        </div>
      </div>
    );
  }
}

export default Select;
