//@flow
import React from "react";
import Notification from "./Notification";

type Props = {
  error?: Error
};

class ErrorNotification extends React.Component<Props> {
  render() {
    const { error } = this.props;
    if (error) {
      return (
        <Notification type="danger">
          <strong>Error:</strong> {error.message}
        </Notification>
      );
    }
    return "";
  }
}

export default ErrorNotification;
