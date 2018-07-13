//@flow
import React from "react";
import { connect } from "react-redux";
import { Redirect } from "react-router-dom";

import { logout, isAuthenticated } from "../modules/auth";
import ErrorPage from "../components/ErrorPage";
import Loading from "../components/Loading";

type Props = {
  loading: boolean,
  authenticated: boolean,
  error?: Error,
  logout: () => void
};

class Logout extends React.Component<Props> {
  componentDidMount() {
    this.props.logout();
  }

  render() {
    const { authenticated, loading, error } = this.props;
    // TODO logout is called twice
    if (error) {
      return (
        <ErrorPage
          title="Logout failed"
          subtitle="Something went wrong durring logout"
          error={error}
        />
      );
    } else if (loading || authenticated) {
      return <Loading />;
    } else {
      return <Redirect to="/login" />;
    }
  }
}

const mapStateToProps = state => {
  let mapped = state.auth.logout || {};
  mapped.authenticated = isAuthenticated(state);
  return mapped;
};

const mapDispatchToProps = dispatch => {
  return {
    logout: () => dispatch(logout())
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Logout);
