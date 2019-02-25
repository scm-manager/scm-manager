//@flow
import React from "react";
import { Redirect, withRouter } from "react-router-dom";
import injectSheet from "react-jss";
import { translate } from "react-i18next";
import {
  login,
  isAuthenticated,
  isLoginPending,
  getLoginFailure
} from "../modules/auth";
import { connect } from "react-redux";

import {
  InputField,
  SubmitButton,
  ErrorNotification,
  Image, UnauthorizedError
} from "@scm-manager/ui-components";
import classNames from "classnames";
import { getLoginLink } from "../modules/indexResource";

const styles = {
  avatar: {
    marginTop: "-70px",
    paddingBottom: "20px"
  },
  avatarImage: {
    border: "1px solid lightgray",
    padding: "5px",
    background: "#fff",
    borderRadius: "50%",
    width: "128px",
    height: "128px"
  },
  avatarSpacing: {
    marginTop: "5rem"
  }
};

type Props = {
  authenticated: boolean,
  loading: boolean,
  error: Error,
  link: string,

  // dispatcher props
  login: (link: string, username: string, password: string) => void,

  // context props
  t: string => string,
  classes: any,
  from: any,
  location: any
};

type State = {
  username: string,
  password: string
};

class Login extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { username: "", password: "" };
  }

  handleUsernameChange = (value: string) => {
    this.setState({ username: value });
  };

  handlePasswordChange = (value: string) => {
    this.setState({ password: value });
  };

  handleSubmit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.login(
        this.props.link,
        this.state.username,
        this.state.password
      );
    }
  };

  isValid() {
    return this.state.username && this.state.password;
  }

  isInValid() {
    return !this.isValid();
  }

  areCredentialsInvalid() {
    const { t, error } = this.props;
    if (error instanceof UnauthorizedError) {
      return new Error(t("error-notification.wrong-login-credentials"));
    } else {
      return error;
    }
  }

  renderRedirect = () => {
    const { from } = this.props.location.state || { from: { pathname: "/" } };
    return <Redirect to={from} />;
  };

  render() {
    const { authenticated, loading, t, classes } = this.props;

    if (authenticated) {
      return this.renderRedirect();
    }

    return (
      <section className="hero">
        <div className="hero-body">
          <div className="container has-text-centered">
            <div className="column is-4 is-offset-4">
              <h3 className="title">{t("login.title")}</h3>
              <p className="subtitle">{t("login.subtitle")}</p>
              <div className={classNames("box", classes.avatarSpacing)}>
                <figure className={classes.avatar}>
                  <Image
                    className={classes.avatarImage}
                    src="/images/blib.jpg"
                    alt={t("login.logo-alt")}
                  />
                </figure>
                <ErrorNotification error={this.areCredentialsInvalid()} />
                <form onSubmit={this.handleSubmit}>
                  <InputField
                    placeholder={t("login.username-placeholder")}
                    autofocus={true}
                    onChange={this.handleUsernameChange}
                  />
                  <InputField
                    placeholder={t("login.password-placeholder")}
                    type="password"
                    onChange={this.handlePasswordChange}
                  />
                  <SubmitButton
                    label={t("login.submit")}
                    fullWidth={true}
                    loading={loading}
                  />
                </form>
              </div>
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
  return {
    authenticated,
    loading,
    error,
    link
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
  )(translate("commons")(Login))
);
export default withRouter(StyledLogin);
