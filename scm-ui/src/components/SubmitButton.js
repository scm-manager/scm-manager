//@flow
import React from "react";
import Logo from "./Logo";
import classNames from "classnames";

type Props = {
  value: string,
  large?: boolean,
  fullWidth?: boolean
};

class SubmitButton extends React.Component<Props> {
  render() {
    const { value, large, fullWidth } = this.props;

    const largeClass = large ? "is-large" : "";
    const fullWidthClass = fullWidth ? "is-fullwidth" : "";

    return (
      <div className="field">
        <div className="control">
          <input
            type="submit"
            className={classNames(
              "button",
              "is-link",
              largeClass,
              fullWidthClass
            )}
            value={value}
          />
        </div>
      </div>
    );
  }
}

export default SubmitButton;
