//@flow
import React from "react";
import Image from "../images/logo.png";

type Props = {
  label?: string,
  placeholder?: string,
  type?: string,
  onChange: string => void
};

class InputField extends React.Component<Props> {
  static defaultProps = {
    type: "text",
    placeholder: ""
  };

  handleInput = (event: SyntheticInputEvent<HTMLInputElement>) => {
    this.props.onChange(event.target.value);
  };

  renderLabel = () => {
    const label = this.props.label;
    if (label) {
      return <label class="label">{label}</label>;
    }
    return "";
  };

  render() {
    const { type, placeholder } = this.props;

    return (
      <div class="field">
        {this.renderLabel()}
        <div class="control">
          <input
            class="input"
            type={type}
            placeholder={placeholder}
            onChange={this.handleInput}
          />
        </div>
      </div>
    );
  }
}

export default InputField;
