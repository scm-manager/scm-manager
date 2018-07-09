import React, { Component } from "react";
import Navigation from "./Navigation";
import Main from "./Main";
import Login from "./Login";
import { getIsAuthenticated } from "../modules/login";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";

type Props = {
  login: boolean,
  username: string,
  getAuthState: any
};

class App extends Component {
  componentWillMount() {
    this.props.getAuthState();
  }
  render() {
    const { login, username } = this.props.login;

    if (!login) {
      return (
        <div>
          <Login />
        </div>
      );
    } else {
      return (
        <div className="App">
          <h2>Welcome, {username}!</h2>
          <Navigation />
          <Main />
        </div>
      );
    }
  }
}

const mapDispatchToProps = dispatch => {
  return {
    getAuthState: () => dispatch(getIsAuthenticated())
  };
};

const mapStateToProps = state => {
  return { login: state.login };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(App)
);
