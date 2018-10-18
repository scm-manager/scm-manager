//@flow
import React from "react";
import classNames from "classnames";
import { withRouter } from "react-router-dom";

export type ButtonProps = {
  label: string,
  loading?: boolean,
  disabled?: boolean,
  action?: (event: Event) => void,
  link?: string,
  fullWidth?: boolean,
  className?: string,
  classes: any
};

type Props = ButtonProps & {
  type: string,
  color: string,

  // context prop
  history: any
};

class Button extends React.Component<Props> {
  static defaultProps = {
    type: "button",
    color: "default"
  };

  onClick = (event: Event) => {
    const { action, link, history } = this.props;
    if (action) {
      action(event);
    } else if (link) {
      history.push(link);
    }
  };

  render() {
    const {
      label,
      loading,
      disabled,
      type,
      color,
      fullWidth,
      className
    } = this.props;
    const loadingClass = loading ? "is-loading" : "";
    const fullWidthClass = fullWidth ? "is-fullwidth" : "";
    return (
      <button
        type={type}
        disabled={disabled}
        onClick={this.onClick}
        className={classNames(
          "button",
          "is-" + color,
          loadingClass,
          fullWidthClass,
          className
        )}
      >
        {label}
      </button>
    );
  };

}

export default withRouter(Button);
