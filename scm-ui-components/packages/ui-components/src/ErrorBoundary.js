// @flow
import React from "react";
import ErrorNotification from "./ErrorNotification";

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { error: null, errorInfo: null };
  }

  componentDidCatch(error, errorInfo) {
    // Catch errors in any components below and re-render with error message
    this.setState({
      error: error,
      errorInfo: errorInfo
    });
  }

  render() {
    if (this.state.errorInfo) {
      return (
        <ErrorNotification error={this.state.error} />
      );
    }
    return this.props.children;
  }
}
export default ErrorBoundary;
