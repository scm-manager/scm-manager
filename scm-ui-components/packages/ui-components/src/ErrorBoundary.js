// @flow
import * as React from "react";
import ErrorNotification from "./ErrorNotification";

type Props = {
  fallback?: React.ComponentType<any>,
  children: React.Node
};

type ErrorInfo = {
  componentStack: string
};

type State = {
  error?: Error,
  errorInfo?: ErrorInfo
};

class ErrorBoundary extends React.Component<Props,State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Catch errors in any components below and re-render with error message
    this.setState({
      error,
      errorInfo
    });
  }

  renderError = () => {
    let FallbackComponent = this.props.fallback;
    if (!FallbackComponent) {
      FallbackComponent = ErrorNotification;
    }

    return <FallbackComponent {...this.state} />;
  };

  render() {
    const { error } = this.state;
    if (error) {
      return this.renderError();
    }
    return this.props.children;
  }
}
export default ErrorBoundary;
