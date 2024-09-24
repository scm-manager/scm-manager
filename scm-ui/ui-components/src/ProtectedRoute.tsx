/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { Component } from "react";
import { Redirect, Route, RouteComponentProps, RouteProps, withRouter } from "react-router-dom";

type Props = RouteComponentProps &
  RouteProps & {
    authenticated?: boolean;
  };

class ProtectedRoute extends Component<Props> {
  constructor(props: Props) {
    super(props);
    this.state = {
      error: undefined,
    };
  }

  renderRoute = (Component: any) => {
    const { authenticated } = this.props;

    return (routeProps: any) => {
      if (authenticated) {
        return <Component {...routeProps} />;
      } else {
        return (
          <Redirect
            to={{
              pathname: "/login",
              state: {
                from: routeProps.location,
              },
            }}
          />
        );
      }
    };
  };

  render() {
    const { component, ...routeProps } = this.props;
    return <Route {...routeProps} render={this.renderRoute(component)} />;
  }
}

export default withRouter(ProtectedRoute);
