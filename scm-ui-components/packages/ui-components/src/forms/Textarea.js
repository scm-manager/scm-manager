//@flow
import React from "react";
import LabelWithHelpIcon from "./LabelWithHelpIcon";

export type SelectItem = {
  value: string,
  label: string
};

type Props = {
  name?: string,
  label?: string,
  placeholder?: SelectItem[],
  value?: string,
  autofocus?: boolean,
  onChange: (value: string, name?: string) => void,
  helpText?: string,
  disabled?: boolean
};

class Textarea extends React.Component<Props> {
  field: ?HTMLTextAreaElement;

  componentDidMount() {
    if (this.props.autofocus && this.field) {
      this.field.focus();
    }
  }

  handleInput = (event: SyntheticInputEvent<HTMLTextAreaElement>) => {
    this.props.onChange(event.target.value, this.props.name);
  };

  render() {
    const { placeholder, value, label, helpText, disabled } = this.props;

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
            disabled={!!disabled}
          />
        </div>
      </div>
    );
  }
}

export default Textarea;
