//@flow
import React from "react";
import injectSheet from "react-jss";
import { login } from "../modules/login";
import { connect } from "react-redux";

import InputField from "../components/InputField";
import SubmitButton from "../components/SubmitButton";

import classNames from "classnames";
import Avatar from "../images/blib.jpg";
import ErrorNotification from "../components/ErrorNotification";

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
  loading: boolean,
  error: Error,
  classes: any,
  login: (username: string, password: string) => void
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
      this.props.login(this.state.username, this.state.password);
    }
  };

  isValid() {
    return this.state.username && this.state.password;
  }

  isInValid() {
    return !this.isValid();
  }

  render() {
    const { classes, loading, error } = this.props;

    return (
      <section className="hero has-background-light">
        <div className="hero-body">
          <div className="container has-text-centered">
            <div className="column is-4 is-offset-4">
              <h3 className="title">Login</h3>
              <p className="subtitle">Please login to proceed.</p>
              <div className={classNames("box", classes.avatarSpacing)}>
                <figure className={classes.avatar}>
                  <img
                    className={classes.avatarImage}
                    src={Avatar}
                    alt="SCM-Manager"
                  />
                </figure>
                <ErrorNotification error={error} />
                <form onSubmit={this.handleSubmit}>
                  <InputField
                    placeholder="Your Username"
                    autofocus={true}
                    onChange={this.handleUsernameChange}
                  />
                  <InputField
                    placeholder="Your Password"
                    type="password"
                    onChange={this.handlePasswordChange}
                  />
                  <SubmitButton
                    value="Login"
                    disabled={this.isInValid()}
                    fullWidth={true}
                    isLoading={loading}
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
  return state.login || {};
};

const mapDispatchToProps = dispatch => {
  return {
    login: (username: string, password: string) =>
      dispatch(login(username, password))
  };
};

const StyledLogin = injectSheet(styles)(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(Login)
);
export default StyledLogin;
