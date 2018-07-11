//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";

import { Route, withRouter } from "react-router";

import Repositories from "../repositories/containers/Repositories";
import Users from "../users/containers/Users";
import { Switch } from "react-router-dom";

const styles = {
  content: {
    paddingTop: "60px"
  }
};

type Props = {
  classes: any
};

class Main extends React.Component<Props> {
  render() {
    const { classes } = this.props;
    return (
      <div className={classNames("container", classes.content)}>
        <Switch>
          <Route exact path="/" component={Repositories} />
          <Route path="/users" component={Users} />
        </Switch>
      </div>
    );
  }
}

export default withRouter(injectSheet(styles)(Main));
