//@flow
import React from "react";

export type SelectItem = {
  value: string,
  label: string
};

type Props = {
  label?: string,
  options: SelectItem[],
  value?: SelectItem,
  onChange: string => void
};

class Select extends React.Component<Props> {
  field: ?HTMLSelectElement;

  handleInput = (event: SyntheticInputEvent<HTMLSelectElement>) => {
    this.props.onChange(event.target.value);
  };

  renderLabel = () => {
    const label = this.props.label;
    if (label) {
      return <label className="label">{label}</label>;
    }
    return "";
  };

  render() {
    const { options, value } = this.props;

    return (
      <div className="field">
        {this.renderLabel()}
        <div className="control select">
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
