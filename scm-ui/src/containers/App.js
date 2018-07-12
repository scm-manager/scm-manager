import React, { Component } from "react";
import type { ThunkDispatch } from "redux-thunk";
import Main from "./Main";
import Login from "./Login";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { fetchMe } from "../modules/me";
import { logout } from "../modules/auth";

import "./App.css";
import "../components/ConfirmAlert.css";
import Header from "../components/Header";
import PrimaryNavigation from "../components/PrimaryNavigation";
import Loading from "../components/Loading";
import ErrorNotification from "../components/ErrorNotification";
import ErrorPage from "../components/ErrorPage";

type Props = {
  me: any,
  error: Error,
  loading: boolean,
  fetchMe: () => void,
  logout: () => void
};

class App extends Component<Props> {
  componentDidMount() {
    this.props.fetchMe();
  }

  logout = () => {
    this.props.logout();
  };

  render() {
    const { me, loading, error } = this.props;

    let content;
    let navigation;

    if (loading) {
      content = <Loading />;
    } else if (error) {
      // TODO add error page instead of plain notification
      content = (
        <ErrorPage
          title="Error"
          subtitle="Unknown error occurred"
          error={error}
        />
      );
    } else if (!me) {
      content = <Login />;
    } else {
      content = <Main me={me} />;
      navigation = <PrimaryNavigation onLogout={this.logout} />;
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
    fetchMe: () => dispatch(fetchMe()),
    logout: () => dispatch(logout())
  };
};

const mapStateToProps = state => {
  return state.me || {};
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(App)
);
