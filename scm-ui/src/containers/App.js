import React, { Component } from "react";
import Navigation from "./Navigation";
import Main from "./Main";
import Login from "./Login";
import { withRouter } from "react-router-dom";

type Props = {
  login: boolean
}

class App extends Component {

  render() {

    const { login} = this.props;

    if(login) {
      return (
        <div>
          <Login/>
        </div>
      );
    }
    else {
      return (
      <div className="App">
        <Navigation />
        <Main />
      </div>
    );
  }
  }
}

export default withRouter(App);
