//@flow
import React from "react";
import { translate } from "react-i18next";
import { Image, ErrorNotification, InputField, SubmitButton, UnauthorizedError } from "@scm-manager/ui-components";
import classNames from "classnames";
import injectSheet from "react-jss";

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
  error?: Error,
  loading: boolean,
  login: (username: string, password: string) => void,

  // context props
  t: string => string,
  classes: any
};

type State = {
  username: string,
  password: string
};

class LoginForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = { username: "", password: "" };
  }

  handleSubmit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.login(
        this.state.username,
        this.state.password
      );
    }
  };

  handleUsernameChange = (value: string) => {
    this.setState({ username: value });
  };

  handlePasswordChange = (value: string) => {
    this.setState({ password: value });
  };

  isValid() {
    return this.state.username && this.state.password;
  }

  areCredentialsInvalid() {
    const { t, error } = this.props;
    if (error instanceof UnauthorizedError) {
      return new Error(t("errorNotification.wrongLoginCredentials"));
    } else {
      return error;
    }
  }

  render() {
    const { loading, classes, t } = this.props;
    return (
      <div className="column is-4 box has-text-centered has-background-white-ter">
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
          <ErrorNotification error={this.areCredentialsInvalid()}/>
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
    );
  }

}

export default injectSheet(styles)(translate("commons")(LoginForm));


