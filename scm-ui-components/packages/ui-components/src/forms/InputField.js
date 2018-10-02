//@flow
import React from "react";
import classNames from "classnames";
import {Help, Page} from "../index";

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
  disabled?: boolean,
  helpText?: string
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

  renderHelp = () => {
    const helpText = this.props.helpText;
    if(helpText){
      return (
        <div className="control columns is-vcentered">
          <Help message={helpText} />
        </div>);
    }
    else
      return null;
  }

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
        <div className="field is-grouped">
          <div className="control is-expanded">
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
          {this.renderHelp()}
        </div>
        {helper}
      </div>
    );
  }
}

export default InputField;
