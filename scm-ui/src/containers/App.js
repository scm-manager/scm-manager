import React, { Component } from "react";
import Main from "./Main";
import Login from "./Login";
import { getIsAuthenticated } from "../modules/login";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { ThunkDispatch } from "redux-thunk";

import "./App.css";
import Header from "../components/Header";
import PrimaryNavigation from "../components/PrimaryNavigation";

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
    const { login, loading } = this.props;

    let content;
    let navigation;

    if (loading) {
      content = <div>Loading...</div>;
    } else if (!login) {
      content = <Login />;
    } else {
      content = <Main />;
      navigation = <PrimaryNavigation />;
    }

    return (
      <div className="App">
        <Header>{navigation}</Header>
        {content}
      </div>
    );
  }
}

const mapDispatchToProps = (dispatch: ThunkDispatch) => {
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
