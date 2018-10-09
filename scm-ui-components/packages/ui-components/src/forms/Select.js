//@flow
import React from "react";
import classNames from "classnames";
import { LabelWithHelpIcon } from "../index";

export type SelectItem = {
  value: string,
  label: string
};

type Props = {
  label?: string,
  options: SelectItem[],
  value?: SelectItem,
  onChange: string => void,
  loading?: boolean,
  helpText?: string
};

class Select extends React.Component<Props> {
  field: ?HTMLSelectElement;

  componentDidMount() {
    // trigger change after render, if value is null to set it to the first value
    // of the given options.
    if (!this.props.value && this.field && this.field.value) {
      this.props.onChange(this.field.value);
    }
  }

  handleInput = (event: SyntheticInputEvent<HTMLSelectElement>) => {
    this.props.onChange(event.target.value);
  };

  render() {
    const { options, value, label, helpText, loading } = this.props;
    const loadingClass = loading ? "is-loading" : "";


    return (
      <div className="field">
        <LabelWithHelpIcon label={label} helpText={helpText} />
    <div className={classNames(
      "control select",
      loadingClass
    )}>
          <select
            ref={input => {
              this.field = input;
            }}
            value={value}
            onChange={this.handleInput}
          >
            {options.map(opt => {
              return (
                <option value={opt.value} key={opt.value}>
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
