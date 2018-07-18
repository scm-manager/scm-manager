//@flow
import React from "react";
import classNames from "classnames";

export type ButtonProps = {
  label: string,
  loading?: boolean,
  disabled?: boolean,
  action: () => void
};

type Props = ButtonProps & {
  type: string
};

class Button extends React.Component<Props> {
  render() {
    const { label, loading, disabled, type, action } = this.props;
    const loadingClass = loading ? "is-loading" : "";
    return (
      <button
        disabled={disabled}
        onClick={action}
        className={classNames("button", "is-" + type, loadingClass)}
      >
        {label}
      </button>
    );
  }
}

export default Button;
