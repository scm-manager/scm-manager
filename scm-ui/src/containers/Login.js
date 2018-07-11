//@flow
import React from "react";
import injectSheet from "react-jss";
import { login } from "../modules/login";
import { connect } from "react-redux";

import InputField from "../components/InputField";
import SubmitButton from "../components/SubmitButton";

import classNames from "classnames";
import Avatar from "../images/blib.jpg";

const styles = {
  spacing: {
    paddingTop: "5rem"
  },
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

  handleSubmit(event: Event) {
    event.preventDefault();
    this.props.login(this.state.username, this.state.password);
  }

  render() {
    const { classes } = this.props;
    return (
      <section className="hero is-fullheight">
        <div className={classes.spacing}>
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
                <form onSubmit={this.handleSubmit.bind(this)}>
                  <InputField
                    placeholder="Your Username"
                    onChange={this.handleUsernameChange}
                  />
                  <InputField
                    placeholder="Your Password"
                    type="password"
                    onChange={this.handlePasswordChange}
                  />
                  <SubmitButton value="Login" fullWidth={true} />
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
