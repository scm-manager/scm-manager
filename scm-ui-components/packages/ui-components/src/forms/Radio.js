//@flow
import React from "react";
import { Help } from "../index";

type Props = {
  label?: string,
  name?: string,
  value?: string,
  checked: boolean,
  onChange?: (value: boolean, name?: string) => void,
  disabled?: boolean,
  helpText?: string
};

class Radio extends React.Component<Props> {
  renderHelp = () => {
    const helpText = this.props.helpText;
    if (helpText) {
      return <Help message={helpText} />;
    }
  };

  render() {
    return (
      <div className="field is-grouped">
        <div className="control">
          <label className="radio" disabled={this.props.disabled}>
            <input
              type="radio"
              name={this.props.name}
              value={this.props.value}
              checked={this.props.checked}
              onChange={this.props.onChange}
              disabled={this.props.disabled}
            />{" "}
            {this.props.label}
            {this.renderHelp()}
          </label>
        </div>
      </div>
    );
  }
}

export default Radio;
