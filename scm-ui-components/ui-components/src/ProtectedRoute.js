//@flow
import React, { Component } from "react";
import { Route, Redirect, withRouter } from "react-router-dom";

type Props = {
  authenticated?: boolean,
  component: Component<any, any>
};

class ProtectedRoute extends React.Component<Props> {
  renderRoute = (Component: any, authenticated?: boolean) => {
    return (routeProps: any) => {
      if (authenticated) {
        return <Component {...routeProps} />;
      } else {
        return (
          <Redirect
            to={{
              pathname: "/login",
              state: { from: routeProps.location }
            }}
          />
        );
      }
    };
  };

  render() {
    const { component, authenticated, ...routeProps } = this.props;
    return (
      <Route
        {...routeProps}
        render={this.renderRoute(component, authenticated)}
      />
    );
  }
}

export default withRouter(ProtectedRoute);
