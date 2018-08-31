//@flow
import React from "react";

type Props = {
  label?: string,
  checked: boolean,
  onChange?: boolean => void,
  disabled?: boolean
};
class Checkbox extends React.Component<Props> {
  onCheckboxChange = (event: SyntheticInputEvent<HTMLInputElement>) => {
    if (this.props.onChange) {
      this.props.onChange(event.target.checked);
    }
  };

  render() {
    return (
      <div className="field">
        <div className="control">
          <label className="checkbox" disabled={this.props.disabled}>
            <input
              type="checkbox"
              checked={this.props.checked}
              onChange={this.onCheckboxChange}
              disabled={this.props.disabled}
            />
            {this.props.label}
          </label>
        </div>
      </div>
    );
  }
}

export default Checkbox;
