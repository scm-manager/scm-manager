//@flow
import React from "react";
import { Redirect, withRouter } from "react-router-dom";
import {
  login,
  isAuthenticated,
  isLoginPending,
  getLoginFailure
} from "../modules/auth";
import { connect } from "react-redux";
import { getLoginLink, getLoginInfoLink } from "../modules/indexResource";
import LoginInfo from "../components/LoginInfo";
import classNames from "classnames";
import injectSheet from "react-jss";

const styles = {
  section: {
    paddingTop: "2em"
  }
};

type Props = {
  authenticated: boolean,
  loading: boolean,
  error?: Error,
  link: string,
  loginInfoLink?: string,

  // dispatcher props
  login: (link: string, username: string, password: string) => void,

  // context props
  classes: any,
  t: string => string,
  from: any,
  location: any
};

class Login extends React.Component<Props> {

  handleLogin = (username: string, password: string): void => {
    const { link, login } = this.props;
    login(link, username, password);
  };

  renderRedirect = () => {
    const { from } = this.props.location.state || { from: { pathname: "/" } };
    return <Redirect to={from}/>;
  };

  render() {
    const { authenticated, classes, ...restProps } = this.props;

    if (authenticated) {
      return this.renderRedirect();
    }

    return (
      <section className={classNames("hero", classes.section )}>
        <div className="hero-body">
          <div className="container">
            <div className="columns is-centered">
              <LoginInfo loginHandler={this.handleLogin} {...restProps} />
            </div>
          </div>
        </div>
      </section>
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
    login: (loginLink: string, username: string, password: string) =>
      dispatch(login(loginLink, username, password))
  };
};

const StyledLogin = injectSheet(styles)(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(Login)
);
export default withRouter(StyledLogin);
