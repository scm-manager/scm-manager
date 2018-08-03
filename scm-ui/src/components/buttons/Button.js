//@flow
import React from "react";
import classNames from "classnames";
import { Link } from "react-router-dom";

export type ButtonProps = {
  label: string,
  loading?: boolean,
  disabled?: boolean,
  action?: (event: Event) => void,
  link?: string,
  fullWidth?: boolean,
  className?: string
};

type Props = ButtonProps & {
  type: string
};

class Button extends React.Component<Props> {
  static defaultProps = {
    type: "default"
  };

  renderButton = () => {
    const {
      label,
      loading,
      disabled,
      type,
      action,
      fullWidth,
      className
    } = this.props;
    const loadingClass = loading ? "is-loading" : "";
    const fullWidthClass = fullWidth ? "is-fullwidth" : "";
    return (
      <button
        disabled={disabled}
        onClick={action ? action : (event: Event) => {}}
        className={classNames(
          "button",
          "is-" + type,
          loadingClass,
          fullWidthClass,
          className
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
