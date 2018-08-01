//@flow
import * as React from "react";
import classNames from "classnames";

type NotificationType = "primary" | "info" | "success" | "warning" | "danger";

type Props = {
  type: NotificationType,
  onClose?: () => void,
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
    const { type, children } = this.props;
    return (
      <div className={classNames("notification", "is-" + type)}>
        {this.renderCloseButton()}
        {children}
      </div>
    );
  }
}

export default Notification;
