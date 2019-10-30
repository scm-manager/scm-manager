import React, { ChangeEvent } from "react";
import { Help } from "../index";
import LabelWithHelpIcon from "./LabelWithHelpIcon";

type Props = {
  label?: string;
  onChange?: (value: boolean, name?: string) => void;
  checked: boolean;
  name?: string;
  title?: string;
  disabled?: boolean;
  helpText?: string;
};

export default class Checkbox extends React.Component<Props> {
  onCheckboxChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (this.props.onChange) {
      this.props.onChange(event.target.checked, this.props.name);
    }
  };

  renderHelp = () => {
    const { title, helpText } = this.props;
    if (helpText && !title) {
      return <Help message={helpText} />;
    }
  };

  renderLabelWithHelp = () => {
    const { title, helpText } = this.props;
    if (title) {
      return <LabelWithHelpIcon label={title} helpText={helpText} />;
    }
  }

  render() {
    const { label, checked, disabled } = this.props;
    return (
      <div className="field">
        {this.renderLabelWithHelp()}
        <div className="control">
          {/*
            we have to ignore the next line,
            because jsx label does not the custom disabled attribute
            but bulma does.
            // @ts-ignore */}
          <label className="checkbox" disabled={disabled}>
            <input
              type="checkbox"
              checked={checked}
              onChange={this.onCheckboxChange}
              disabled={disabled}
            />{" "}
            {label}
            {this.renderHelp()}
          </label>
        </div>
      </div>
    );
  }
}
