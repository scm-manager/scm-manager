import React, { MouseEvent, ReactNode } from "react";
import classNames from "classnames";
import { withRouter, RouteComponentProps } from "react-router-dom";
import Icon from "../Icon";

export type ButtonProps = {
  label?: string;
  loading?: boolean;
  disabled?: boolean;
  action?: (event: MouseEvent) => void;
  link?: string;
  className?: string;
  icon?: string;
  fullWidth?: boolean;
  reducedMobile?: boolean;
  children?: ReactNode;
};

type Props = ButtonProps &
  RouteComponentProps & {
    type?: "button" | "submit" | "reset";
    color?: string;
  };

class Button extends React.Component<Props> {
  static defaultProps: Partial<Props> = {
    type: "button",
    color: "default"
  };

  onClick = (event: React.MouseEvent) => {
    const { action, link, history } = this.props;
    if (action) {
      action(event);
    } else if (link) {
      history.push(link);
    }
  };

  render() {
    const { label, loading, disabled, type, color, className, icon, fullWidth, reducedMobile, children } = this.props;
    const loadingClass = loading ? "is-loading" : "";
    const fullWidthClass = fullWidth ? "is-fullwidth" : "";
    const reducedMobileClass = reducedMobile ? "is-reduced-mobile" : "";
    if (icon) {
      return (
        <button
          type={type}
          disabled={disabled}
          onClick={this.onClick}
          className={classNames("button", "is-" + color, loadingClass, fullWidthClass, reducedMobileClass, className)}
        >
          <span className="icon is-medium">
            <Icon name={icon} color="inherit" />
          </span>
          <span>
            {label} {children}
          </span>
        </button>
      );
    }

    return (
      <button
        type={type}
        disabled={disabled}
        onClick={this.onClick}
        className={classNames("button", "is-" + color, loadingClass, fullWidthClass, className)}
      >
        {label} {children}
      </button>
    );
  }
}

export default withRouter(Button);
