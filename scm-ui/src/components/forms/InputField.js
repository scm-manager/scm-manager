//@flow
import React from "react";

type Props = {
  label?: string,
  placeholder?: string,
  value?: string,
  type?: string,
  autofocus?: boolean,
  onChange: string => void
};

class InputField extends React.Component<Props> {
  static defaultProps = {
    type: "text",
    placeholder: ""
  };

  field: ?HTMLInputElement;

  componentDidMount() {
    if (this.props.autofocus && this.field) {
      this.field.focus();
    }
  }

  handleInput = (event: SyntheticInputEvent<HTMLInputElement>) => {
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
    const { type, placeholder, value } = this.props;

    return (
      <div className="field">
        {this.renderLabel()}
        <div className="control">
          <input
            ref={input => {
              this.field = input;
            }}
            className="input"
            type={type}
            placeholder={placeholder}
            value={value}
            onChange={this.handleInput}
          />
        </div>
      </div>
    );
  }
}

export default InputField;
