//@flow
import React from "react";
import classNames from "classnames";
import { Link } from "react-router-dom";

export type ButtonProps = {
  label: string,
  loading?: boolean,
  disabled?: boolean,
  action?: () => void,
  link?: string,
  fullWidth?: boolean
};

type Props = ButtonProps & {
  type: string
};

class Button extends React.Component<Props> {
  renderButton = () => {
    const { label, loading, disabled, type, action, fullWidth } = this.props;
    const loadingClass = loading ? "is-loading" : "";
    const fullWidthClass = fullWidth ? "is-fullwidth" : "";
    return (
      <button
        disabled={disabled}
        onClick={action ? action : () => {}}
        className={classNames(
          "button",
          "is-" + type,
          loadingClass,
          fullWidthClass
        )}
      >
        {label}
      </button>
    );
  };

  render() {
    const { link } = this.props;
    if (link) {
      return <Link to={link}>{this.renderButton()}</Link>;
    } else {
      return this.renderButton();
    }
  }
}

export default Button;
