//@flow
import React from "react";
import injectSheet from "react-jss";
import { login } from "../modules/login";
import { connect } from "react-redux";

const styles = {
  wrapper: {
    width: "100%",
    display: "flex",
    height: "10em"
  },
  login: {
    margin: "auto",
    textAlign: "center"
  }
};

class Login extends React.Component<Props> {
  state = {};
  handleUsernameChange(event) {
    this.setState({ username: event.target.value });
  }

  handlePasswordChange(event) {
    this.setState({ password: event.target.value });
  }

  handleSubmit(event) {
    event.preventDefault();
    this.props.login(this.state.username, this.state.password);
  }

  render() {
    const { classes } = this.props;
    return (
      <div className={classes.wrapper}>
        <div className={classes.login}>
          You need to log in! ...
          <form onSubmit={this.handleSubmit.bind(this)}>
            <input
              type="text"
              defaultValue="Username"
              onChange={this.handleUsernameChange.bind(this)}
            />
            <input
              type="password"
              defaultValue="Password"
              onChange={this.handlePasswordChange.bind(this)}
            />
            <input type="submit" value="Login" />
          </form>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => {
  return {};
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
