//@flow
import React from "react";

import { Redirect, Route, Switch, withRouter } from "react-router-dom";
import type { Links } from "@scm-manager/ui-types";

import Overview from "../repos/containers/Overview";
import Users from "../users/containers/Users";
import Login from "../containers/Login";
import Logout from "../containers/Logout";

import { ProtectedRoute } from "@scm-manager/ui-components";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";

import CreateUser from "../users/containers/CreateUser";
import SingleUser from "../users/containers/SingleUser";
import RepositoryRoot from "../repos/containers/RepositoryRoot";
import Create from "../repos/containers/Create";

import Groups from "../groups/containers/Groups";
import SingleGroup from "../groups/containers/SingleGroup";
import CreateGroup from "../groups/containers/CreateGroup";

import Admin from "../admin/containers/Admin";

import Profile from "./Profile";

type Props = {
  authenticated?: boolean,
  links: Links
};

class Main extends React.Component<Props> {
  render() {
    const { authenticated, links } = this.props;
    const redirectUrlFactory = binder.getExtension("main.redirect", this.props);
    let url = "/repos/";
    if (redirectUrlFactory) {
      url = redirectUrlFactory(this.props);
    }
    return (
      <div className="main">
        <Switch>
          <Redirect exact from="/" to={url} />
          <Route exact path="/login" component={Login} />
          <Route path="/logout" component={Logout} />
          <Redirect exact strict from="/repos" to="/repos/" />
          <ProtectedRoute
            exact
            path="/repos/"
            component={Overview}
            authenticated={authenticated}
          />
          <ProtectedRoute
            exact
            path="/repos/create"
            component={Create}
            authenticated={authenticated}
          />
          <ProtectedRoute
            exact
            path="/repos/:page"
            component={Overview}
            authenticated={authenticated}
          />
          <ProtectedRoute
            path="/repo/:namespace/:name"
            component={RepositoryRoot}
            authenticated={authenticated}
          />
          <Redirect exact strict from="/users" to="/users/" />
          <ProtectedRoute
            exact
            path="/users/"
            component={Users}
            authenticated={authenticated}
          />
          <ProtectedRoute
            authenticated={authenticated}
            path="/users/create"
            component={CreateUser}
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
          <Redirect exact strict from="/groups" to="/groups/" />
          <ProtectedRoute
            exact
            path="/groups/"
            component={Groups}
            authenticated={authenticated}
          />
          <ProtectedRoute
            authenticated={authenticated}
            path="/group/:name"
            component={SingleGroup}
          />
          <ProtectedRoute
            authenticated={authenticated}
            path="/groups/create"
            component={CreateGroup}
          />
          <ProtectedRoute
            exact
            path="/groups/:page"
            component={Groups}
            authenticated={authenticated}
          />
          <ProtectedRoute
            path="/admin"
            component={Admin}
            authenticated={authenticated}
          />
          <ProtectedRoute
            path="/me"
            component={Profile}
            authenticated={authenticated}
          />
          <ExtensionPoint
            name="main.route"
            renderAll={true}
            props={{ authenticated, links }}
          />
        </Switch>
      </div>
    );
  }
}

export default withRouter(Main);
