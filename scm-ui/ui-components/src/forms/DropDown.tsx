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
import styled from "styled-components";

type Props = {
  options: string[];
  optionValues?: string[];
  optionSelected: (p: string) => void;
  preselectedOption?: string;
  className?: string;
  disabled?: boolean;
};

const FullWidthSelect = styled.select`
  width: 100%;
`;

class DropDown extends React.Component<Props> {
  render() {
    const { options, optionValues, preselectedOption, className, disabled } = this.props;

    if (preselectedOption && options.some(o => o === preselectedOption)) {
      options.push(preselectedOption);
    }

    return (
      <div className={classNames(className, "select", disabled ? "disabled" : "")}>
        <FullWidthSelect value={preselectedOption ? preselectedOption : ""} onChange={this.change} disabled={disabled}>
          {options.map((option, index) => {
            const value = optionValues && optionValues[index] ? optionValues[index] : option;
            return (
              <option key={value} value={value} selected={value === preselectedOption}>
                {option}
              </option>
            );
          })}
        </FullWidthSelect>
      </div>
    );
  }

  change = (event: ChangeEvent<HTMLSelectElement>) => {
    this.props.optionSelected(event.target.value);
  };
}

export default DropDown;
