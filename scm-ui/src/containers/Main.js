//@flow
import React from "react";

import { Route, withRouter } from "react-router";

import Repositories from "../repositories/containers/Repositories";
import Users from "../users/containers/Users";

import { Switch } from "react-router-dom";
import Footer from "../components/Footer";

import type { Me } from "../types/me";

type Props = {
  me: Me
};

class Main extends React.Component<Props> {
  render() {
    const { me } = this.props;
    return (
      <div>
        <Switch>
          <Route exact path="/" component={Repositories} />
          <Route path="/users" component={Users} />
        </Switch>
        <Footer me={me} />
      </div>
    );
  }
}

export default withRouter(Main);
