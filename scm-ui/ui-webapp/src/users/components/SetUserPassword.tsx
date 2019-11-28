import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { User } from "@scm-manager/ui-types";
import { Level, SubmitButton, Notification, ErrorNotification, PasswordConfirmation } from "@scm-manager/ui-components";
import { setPassword } from "./setPassword";

type Props = WithTranslation & {
  user: User;
};

type State = {
  password: string;
  loading: boolean;
  error?: Error;
  passwordChanged: boolean;
  passwordValid: boolean;
};

class SetUserPassword extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      password: "",
      loading: false,
      passwordConfirmationError: false,
      validatePasswordError: false,
      validatePassword: "",
      passwordChanged: false,
      passwordValid: false
    };
  }

  setLoadingState = () => {
    this.setState({
      ...this.state,
      loading: true
    });
  };

  setErrorState = (error: Error) => {
    this.setState({
      ...this.state,
      error: error,
      loading: false
    });
  };

  setSuccessfulState = () => {
    this.setState({
      ...this.state,
      loading: false,
      passwordChanged: true,
      password: ""
    });
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.state.password) {
      const { user } = this.props;
      const { password } = this.state;
      this.setLoadingState();
      setPassword(user._links.password.href, password)
        .then(result => {
          if (result.error) {
            this.setErrorState(result.error);
          } else {
            this.setSuccessfulState();
          }
        })
        .catch(err => {});
    }
  };

  render() {
    const { t } = this.props;
    const { loading, passwordChanged, error } = this.state;

    let message = null;

    if (passwordChanged) {
      message = (
        <Notification
          type={"success"}
          children={t("singleUserPassword.setPasswordSuccessful")}
          onClose={() => this.onClose()}
        />
      );
    } else if (error) {
      message = <ErrorNotification error={error} />;
    }

    return (
      <form onSubmit={this.submit}>
        {message}
        <PasswordConfirmation
          passwordChanged={this.passwordChanged}
          key={this.state.passwordChanged ? "changed" : "unchanged"}
        />
        <Level
          right={
            <SubmitButton
              disabled={!this.state.passwordValid}
              loading={loading}
              label={t("singleUserPassword.button")}
            />
          }
        />
      </form>
    );
  }

  passwordChanged = (password: string, passwordValid: boolean) => {
    this.setState({
      ...this.state,
      password,
      passwordValid: !!password && passwordValid
    });
  };

  onClose = () => {
    this.setState({
      ...this.state,
      passwordChanged: false
    });
  };
}

export default withTranslation("users")(SetUserPassword);
