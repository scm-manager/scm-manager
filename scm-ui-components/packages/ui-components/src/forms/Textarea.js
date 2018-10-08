//@flow
import React from "react";
import { LabelWithHelpIcon } from "../index";

export type SelectItem = {
  value: string,
  label: string
};

type Props = {
  label?: string,
  placeholder?: SelectItem[],
  value?: string,
  onChange: string => void,
  helpText?: string
};

class Textarea extends React.Component<Props> {
  field: ?HTMLTextAreaElement;

  handleInput = (event: SyntheticInputEvent<HTMLTextAreaElement>) => {
    this.props.onChange(event.target.value);
  };

  render() {
    const { placeholder, value, label, helpText } = this.props;

    return (
      <div className="field">
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
          <textarea
            className="textarea"
            ref={input => {
              this.field = input;
            }}
            placeholder={placeholder}
            onChange={this.handleInput}
            value={value}
          />
        </div>
      </div>
    );
  }
}

export default Textarea;
