import React, { ChangeEvent } from "react";
import classNames from "classnames";

type Props = {
  options: string[];
  optionValues?: string[];
  optionSelected: (p: string) => void;
  preselectedOption?: string;
  className?: string;
  disabled?: boolean;
};

class DropDown extends React.Component<Props> {
  render() {
    const { options, optionValues, preselectedOption, className, disabled } = this.props;
    return (
      <div className={classNames(className, "select", disabled ? "disabled" : "")}>
        <select value={preselectedOption ? preselectedOption : ""} onChange={this.change} disabled={disabled}>
          {options.map((option, index) => {
            const value = optionValues && optionValues[index] ? optionValues[index] : option;
            return (
              <option key={value} value={value} selected={value === preselectedOption}>
                {option}
              </option>
            );
          })}
        </select>
      </div>
    );
  }

  change = (event: ChangeEvent<HTMLSelectElement>) => {
    this.props.optionSelected(event.target.value);
  };
}

export default DropDown;
