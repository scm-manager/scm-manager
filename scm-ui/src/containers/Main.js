//@flow
import React from "react";

import { Route, withRouter } from "react-router";

import Repositories from "../repositories/containers/Repositories";
import Users from "../users/containers/Users";
import Login from "../containers/Login";
import Logout from "../containers/Logout";

import { Switch } from "react-router-dom";
import ProtectedRoute from "../components/ProtectedRoute";

type Props = {
  authenticated?: boolean
};

class Main extends React.Component<Props> {
  render() {
    const { authenticated } = this.props;
    return (
      <div>
        <Switch>
          <ProtectedRoute
            exact
            path="/"
            component={Repositories}
            authenticated={authenticated}
          />
          <Route exact path="/login" component={Login} />
          <Route path="/logout" component={Logout} />
          <ProtectedRoute
            path="/users"
            component={Users}
            authenticated={authenticated}
          />
        </Switch>
      </div>
    );
  }
}

export default withRouter(Main);
