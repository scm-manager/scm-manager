import React, { Component } from "react";
import Main from "./Main";
import Login from "./Login";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { ThunkDispatch } from "redux-thunk";
import { fetchMe } from "../modules/me";

import "./App.css";
import Header from "../components/Header";
import PrimaryNavigation from "../components/PrimaryNavigation";
import Loading from "../components/Loading";
import Footer from "../components/Footer";
import ErrorNotification from "../components/ErrorNotification";

type Props = {
  me: any,
  error: Error,
  loading: boolean,
  fetchMe: () => void
};

class App extends Component<Props> {
  componentDidMount() {
    this.props.fetchMe();
  }
  render() {
    const { me, loading, error } = this.props;

    let content;
    let navigation;

    if (loading) {
      content = <Loading />;
    } else if (error) {
      // TODO add error page instead of plain notification
      content = <ErrorNotification error={error} />;
    } else if (!me) {
      content = <Login />;
    } else {
      content = <Main me={me} />;
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
    fetchMe: () => dispatch(fetchMe())
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
