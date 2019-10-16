//@flow
import * as React from "react";
import classNames from "classnames";

type NotificationType =
  | "primary"
  | "info"
  | "success"
  | "warning"
  | "danger"
  | "inherit";

type Props = {
  type: NotificationType,
  onClose?: () => void,
  className?: string,
  children?: React.Node
};

class Notification extends React.Component<Props> {
  static defaultProps = {
    type: "info"
  };

  renderCloseButton() {
    const { onClose } = this.props;
    if (onClose) {
      return <button className="delete" onClick={onClose} />;
    }
    return "";
  }

  render() {
    const { type, className, children } = this.props;

    const color = type !== "inherit" ? "is-" + type : "";

    return (
      <div className={classNames("notification", color, className)}>
        {this.renderCloseButton()}
        {children}
      </div>
    );
  }
}

export default Notification;
