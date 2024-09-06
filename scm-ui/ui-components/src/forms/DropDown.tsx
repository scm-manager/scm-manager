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

/**
 * @deprecated Use `Select` instead
 */
class DropDown extends React.Component<Props> {
  render() {
    const { options, optionValues, preselectedOption, className, disabled } = this.props;

    if (preselectedOption && options.filter((o) => o === preselectedOption).length === 0) {
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
