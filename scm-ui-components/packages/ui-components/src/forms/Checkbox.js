//@flow
import React from "react";
import { Help } from "../index";

type Props = {
  label?: string,
  checked: boolean,
  onChange?: boolean => void,
  disabled?: boolean,
  helpText?: string
};
class Checkbox extends React.Component<Props> {
  onCheckboxChange = (event: SyntheticInputEvent<HTMLInputElement>) => {
    if (this.props.onChange) {
      this.props.onChange(event.target.checked);
    }
  };

  renderHelp = () => {
    const helpText = this.props.helpText;
    if (helpText) {
      return (
        <div className="control columns is-vcentered">
          <Help message={helpText} />
        </div>
      );
    } else return null;
  };

  render() {
    return (
      <div className="field is-grouped">
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
        {this.renderHelp()}
      </div>
    );
  }
}

export default Checkbox;
