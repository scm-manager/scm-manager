//@flow
import React from "react";
import Logo from "./Logo";
import classNames from "classnames";

type Props = {
  value: string,
  disabled: boolean,
  isLoading: boolean,
  large?: boolean,
  fullWidth?: boolean
};

class SubmitButton extends React.Component<Props> {
  render() {
    const { value, large, fullWidth, isLoading, disabled } = this.props;

    const largeClass = large ? "is-large" : "";
    const fullWidthClass = fullWidth ? "is-fullwidth" : "";
    const loadingClass = isLoading ? "is-loading" : "";

    return (
      <div className="field">
        <div className="control">
          <button
            type="submit"
            disabled={disabled}
            className={classNames(
              "button",
              "is-link",
              largeClass,
              fullWidthClass,
              loadingClass
            )}
          >
            {value}
          </button>
        </div>
      </div>
    );
  }
}

export default SubmitButton;
