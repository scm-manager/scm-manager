//@flow
import React from "react";
import classNames from "classnames";

type Props = {
  label?: string,
  placeholder?: string,
  value?: string,
  type?: string,
  autofocus?: boolean,
  onChange: string => void,
  onReturnPressed?: () => void,
  validationError: boolean,
  errorMessage: string,
  disabled?: boolean
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


  handleKeyPress = (event: SyntheticKeyboardEvent<HTMLInputElement>) => {
    const onReturnPressed = this.props.onReturnPressed;
    if (!onReturnPressed) {
      return;
    }
    if (event.key === "Enter") {
      event.preventDefault();
      onReturnPressed();
    }
  };

  render() {
    const {
      type,
      placeholder,
      value,
      validationError,
      errorMessage,
      disabled
    } = this.props;
    const errorView = validationError ? "is-danger" : "";
    const helper = validationError ? (
      <p className="help is-danger">{errorMessage}</p>
    ) : (
      ""
    );
    return (
      <div className="field">
        {this.renderLabel()}
        <div className="control">
          <input
            ref={input => {
              this.field = input;
            }}
            className={classNames("input", errorView)}
            type={type}
            placeholder={placeholder}
            value={value}
            onChange={this.handleInput}
            onKeyPress={this.handleKeyPress}
            disabled={disabled}
          />
        </div>
        {helper}
      </div>
    );
  }
}

export default InputField;
