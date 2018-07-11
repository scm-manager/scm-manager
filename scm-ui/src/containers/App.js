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
  getAuthState: () => void,
  loading: boolean
};

class App extends Component<Props> {
  componentDidMount() {
    this.props.getAuthState();
  }
  render() {
    const { login, username, loading } = this.props;
    if (loading) {
      return <div>Loading...</div>;
    } else if (!login) {
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
  return state.login || {};
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(App)
);
