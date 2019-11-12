import React, { MouseEvent } from "react";
import { withRouter, RouteComponentProps } from "react-router-dom";
import classNames from "classnames";
import Icon from "../Icon";

export type ButtonProps = {
  icon: string;
  title?: string;
  loading?: boolean;
  disabled?: boolean;
  action?: (event: MouseEvent) => void;
  link?: string;
  className?: string;
};

type Props = ButtonProps &
  RouteComponentProps & {
    type?: "button" | "submit" | "reset";
    color?: string;
  };

class IconButton extends React.Component<Props> {
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
    const { icon, title, loading, disabled, type, color, className } = this.props;
    const loadingClass = loading ? "is-loading" : "";
    return (
      <button
        className={classNames("button", "is-" + color, loadingClass, className)}
        title={title}
        type={type}
        disabled={disabled}
        onClick={this.onClick}
      >
        <span className="icon is-medium">
          <Icon name={icon} color="inherit" />
        </span>
      </button>
    );
  }
}

export default withRouter(IconButton);
