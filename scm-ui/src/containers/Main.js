//@flow
import React from "react";

import { Route, withRouter } from "react-router";

import Repositories from "../repositories/containers/Repositories";
import Users from "../users/containers/Users";
import Login from "../containers/Login";
import Logout from "../containers/Logout";

import { Switch } from "react-router-dom";
import ProtectedRoute from "../components/ProtectedRoute";
import AddUser from "../users/containers/AddUser";
import SingleUser from "../users/containers/SingleUser";

import Groups from "../groups/containers/Groups";
import AddGroup from "../groups/containers/AddGroup"

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
            exact
            path="/users"
            component={Users}
            authenticated={authenticated}
          />
          <ProtectedRoute
            authenticated={authenticated}
            path="/users/add"
            component={AddUser}
          />
          <ProtectedRoute
            exact
            path="/users/:page"
            component={Users}
            authenticated={authenticated}
          />
          <ProtectedRoute
            authenticated={authenticated}
            path="/user/:name"
            component={SingleUser}
          />
          <ProtectedRoute
            exact
            path="/groups"
            component={Groups}
            authenticated={authenticated}
          />
          <ProtectedRoute
            authenticated={authenticated}
            path="/groups/add"
            component={AddGroup}
          />
        </Switch>
      </div>
    );
  }
}

export default withRouter(Main);
