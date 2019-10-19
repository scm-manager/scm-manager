import React from 'react';
import classNames from 'classnames';
import LabelWithHelpIcon from './LabelWithHelpIcon';

type Props = {
  label?: string;
  name?: string;
  placeholder?: string;
  value?: string;
  type?: string;
  autofocus?: boolean;
  onChange: (value: string, name?: string) => void;
  onReturnPressed?: () => void;
  validationError: boolean;
  errorMessage: string;
  disabled?: boolean;
  helpText?: string;
};

class InputField extends React.Component<Props> {
  static defaultProps = {
    type: 'text',
    placeholder: '',
  };

  field: HTMLInputElement | null | undefined;

  componentDidMount() {
    if (this.props.autofocus && this.field) {
      this.field.focus();
    }
  }

  handleInput = (event: SyntheticInputEvent<HTMLInputElement>) => {
    this.props.onChange(event.target.value, this.props.name);
  };

  handleKeyPress = (event: SyntheticKeyboardEvent<HTMLInputElement>) => {
    const onReturnPressed = this.props.onReturnPressed;
    if (!onReturnPressed) {
      return;
    }
    if (event.key === 'Enter') {
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
      disabled,
      label,
      helpText,
    } = this.props;
    const errorView = validationError ? 'is-danger' : '';
    const helper = validationError ? (
      <p className="help is-danger">{errorMessage}</p>
    ) : (
      ''
    );
    return (
      <div className="field">
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <div className="control">
          <input
            ref={input => {
              this.field = input;
            }}
            className={classNames('input', errorView)}
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
