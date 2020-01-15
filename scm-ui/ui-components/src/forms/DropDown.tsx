import React, { ChangeEvent } from "react";
import classNames from "classnames";

type Props = {
  options: string[];
  optionValues?: string[];
  optionSelected: (p: string) => void;
  preselectedOption?: string;
  className: string;
  disabled?: boolean;
};

class DropDown extends React.Component<Props> {
  render() {
    const { options, optionValues, preselectedOption, className, disabled } = this.props;

    if (preselectedOption && !options.includes(preselectedOption)) {
      options.unshift(preselectedOption);
    }

    return (
      <div className={classNames(className, "select", disabled ? "disabled" : "")}>
        <select value={preselectedOption ? preselectedOption : ""} onChange={this.change} disabled={disabled}>
          <option key={preselectedOption} />
          {options.map((option, index) => {
            return (
              <option key={option} value={optionValues && optionValues[index] ? optionValues[index] : option}>
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
