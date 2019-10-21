import React from "react";
import { connect } from "react-redux";
import { Redirect, withRouter } from "react-router-dom";
import { compose } from "redux";
import { translate } from "react-i18next";
import styled from "styled-components";
import { getLoginFailure, isAuthenticated, isLoginPending, login } from "../modules/auth";
import { getLoginInfoLink, getLoginLink } from "../modules/indexResource";
import LoginInfo from "../components/LoginInfo";

type Props = {
  authenticated: boolean;
  loading: boolean;
  error?: Error;
  link: string;
  loginInfoLink?: string;

  // dispatcher props
  login: (link: string, username: string, password: string) => void;

  // context props
  t: (p: string) => string;
  from: any;
  location: any;
};

const HeroSection = styled.section`
  padding-top: 2em;
`;

class Login extends React.Component<Props> {
  handleLogin = (username: string, password: string): void => {
    const { link, login } = this.props;
    login(link, username, password);
  };

  renderRedirect = () => {
    const { from } = this.props.location.state || {
      from: {
        pathname: "/"
      }
    };
    return <Redirect to={from} />;
  };

  render() {
    const { authenticated, ...restProps } = this.props;

    if (authenticated) {
      return this.renderRedirect();
    }

    return (
      <HeroSection className="hero">
        <div className="hero-body">
          <div className="container">
            <div className="columns is-centered">
              <LoginInfo loginHandler={this.handleLogin} {...restProps} />
            </div>
          </div>
        </div>
      </HeroSection>
    );
  }
}

const mapStateToProps = state => {
  const authenticated = isAuthenticated(state);
  const loading = isLoginPending(state);
  const error = getLoginFailure(state);
  const link = getLoginLink(state);
  const loginInfoLink = getLoginInfoLink(state);
  return {
    authenticated,
    loading,
    error,
    link,
    loginInfoLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    login: (loginLink: string, username: string, password: string) => dispatch(login(loginLink, username, password))
  };
};

export default compose(
  withRouter,
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(Login);
