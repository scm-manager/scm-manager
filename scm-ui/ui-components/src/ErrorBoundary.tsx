/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FC, ReactNode } from "react";
import ErrorNotification from "./ErrorNotification";
import { MissingLinkError } from "./errors";
import { withContextPath } from "./urls";
import { RouteComponentProps, useLocation, withRouter } from "react-router-dom";
import ErrorPage from "./ErrorPage";
import { useTranslation } from "react-i18next";
import { useIndexLink } from "@scm-manager/ui-api";

type ExportedProps = {
  fallback?: React.ComponentType<any>;
  children: ReactNode;
};

type Props = RouteComponentProps & ExportedProps;

type ErrorInfo = {
  componentStack: string;
};

type State = {
  error?: Error;
  errorInfo?: ErrorInfo;
};

type ErrorDisplayProps = {
  fallback?: React.ComponentType<any>;
  error: Error;
  errorInfo: ErrorInfo;
};

const ErrorDisplay: FC<ErrorDisplayProps> = ({ error, errorInfo, fallback: FallbackComponent }) => {
  const loginLink = useIndexLink("login");
  const [t] = useTranslation("commons");
  const location = useLocation();

  if (error instanceof MissingLinkError) {
    if (loginLink) {
      window.location.assign(withContextPath("/login?from=" + location.pathname));
    } else {
      return (
        <ErrorPage error={error} title={t("errorNotification.prefix")} subtitle={t("errorNotification.forbidden")} />
      );
    }
  }

  if (!FallbackComponent) {
    FallbackComponent = ErrorNotification;
  }

  const fallbackProps = {
    error,
    errorInfo
  };

  return <FallbackComponent {...fallbackProps} />;
};

class ErrorBoundary extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidUpdate(prevProps: Readonly<Props>) {
    // we must reset the error if the url has changed
    if (this.state.error && prevProps.location !== this.props.location) {
      this.setState({ error: undefined, errorInfo: undefined });
    }
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    this.setState({
      error,
      errorInfo
    });
  }

  render() {
    const { fallback } = this.props;
    const { error, errorInfo } = this.state;
    if (error && errorInfo) {
      return <ErrorDisplay error={error} errorInfo={errorInfo} fallback={fallback} />;
    }
    return this.props.children;
  }
}

export default withRouter(ErrorBoundary);
