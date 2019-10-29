import React, { ChangeEvent } from "react";
import { Help } from "../index";

type Props = {
  label?: string;
  name?: string;
  checked: boolean;
  onChange?: (value: boolean, name?: string) => void;
  disabled?: boolean;
  helpText?: string;
};

class Checkbox extends React.Component<Props> {
  onCheckboxChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (this.props.onChange) {
      this.props.onChange(event.target.checked, this.props.name);
    }
  };

  renderHelp = () => {
    const helpText = this.props.helpText;
    if (helpText) {
      return <Help message={helpText} />;
    }
  };

  render() {
    return (
      <div className="field">
        <div className="control">
          {/*
            we have to ignore the next line, 
            because jsx label does not the custom disabled attribute
            but bulma does.
            // @ts-ignore */}
          <label className="checkbox" disabled={this.props.disabled}>
            <input
              type="checkbox"
              checked={this.props.checked}
              onChange={this.onCheckboxChange}
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

export default Checkbox;
