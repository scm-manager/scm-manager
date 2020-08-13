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
import React, {Component} from "react";
import {Redirect, Route, RouteComponentProps, RouteProps, withRouter} from "react-router-dom";
import {MissingLinkError} from "./errors";
import ErrorPage from "./ErrorPage";
import {withTranslation, WithTranslation} from "react-i18next";

type Props = WithTranslation &
  RouteComponentProps &
  RouteProps & {
    authenticated?: boolean;
    loginLink?: string;
  };

type State = {
  error?: Error;
};

class ProtectedRoute extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      error: undefined
    };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    if (error instanceof MissingLinkError) {
      this.setState({
        error
      });
    }
  }

  renderRoute = (Component: any, authenticated?: boolean) => {
    const { loginLink, t } = this.props;
    const { error } = this.state;
    return (routeProps: any) => {
      if (error) {
        if (loginLink) {
          return (
            <Redirect
              to={{
                pathname: "/login",
                state: {
                  from: routeProps.location
                }
              }}
            />
          );
        } else {
          return (
            <ErrorPage
              error={error}
              title={t("errorNotification.prefix")}
              subtitle={t("errorNotification.forbidden")}
            />
          );
        }
      } else if (authenticated) {
        return <Component {...routeProps} />;
      } else {
        return (
          <Redirect
            to={{
              pathname: "/login",
              state: {
                from: routeProps.location
              }
            }}
          />
        );
      }
    };
  };

  render() {
    const { component, authenticated, ...routeProps } = this.props;
    return <Route {...routeProps} render={this.renderRoute(component, authenticated)} />;
  }
}

export default withTranslation("commons")(withRouter(ProtectedRoute));
