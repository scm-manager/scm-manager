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
import React, { ReactNode } from "react";
import ErrorNotification from "./ErrorNotification";
import { MissingLinkError } from "./errors";
import { withContextPath } from "./urls";
import { withRouter, RouteComponentProps } from "react-router-dom";
import ErrorPage from "./ErrorPage";
import { WithTranslation, withTranslation } from "react-i18next";
import { compose } from "redux";
import { connect } from "react-redux";

type Props = WithTranslation &
  RouteComponentProps & {
    fallback?: React.ComponentType<any>;
    children: ReactNode;
    loginLink?: string;
  };

type ErrorInfo = {
  componentStack: string;
};

type State = {
  error?: Error;
  errorInfo?: ErrorInfo;
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
    this.setState(
      {
        error,
        errorInfo
      },
      () => this.redirectToLogin(error)
    );
  }

  redirectToLogin = (error: Error) => {
    const { loginLink } = this.props;
    if (error instanceof MissingLinkError) {
      if (loginLink) {
        window.location.assign(withContextPath("/login"));
      }
    }
  };

  renderError = () => {
    const { t } = this.props;
    const { error } = this.state;

    let FallbackComponent = this.props.fallback;

    if (error instanceof MissingLinkError) {
      return (
        <ErrorPage error={error} title={t("errorNotification.prefix")} subtitle={t("errorNotification.forbidden")} />
      );
    }

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

const mapStateToProps = (state: any) => {
  const loginLink = state.indexResources?.links?.login?.href;

  return {
    loginLink
  };
};

export default compose(connect(mapStateToProps), withRouter, withTranslation("commons"))(ErrorBoundary);
