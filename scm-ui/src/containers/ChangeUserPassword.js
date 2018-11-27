// @flow
import React from "react";
import {
  ErrorNotification,
  InputField,
  Notification,
  PasswordConfirmation,
  SubmitButton
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import type { Me } from "@scm-manager/ui-types";
import { changePassword } from "../modules/changePassword";

type Props = {
  me: Me,
  t: string => string
};

type State = {
  oldPassword: string,
  password: string,
  loading: boolean,
  error?: Error,
  passwordChanged: boolean
};

class ChangeUserPassword extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      oldPassword: "",
      password: "",
      loading: false,
      passwordConfirmationError: false,
      validatePasswordError: false,
      validatePassword: "",
      passwordChanged: false
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
      oldPassword: "",
      password: ""
    });
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.state.password) {
      const { oldPassword, password } = this.state;
      this.setLoadingState();
      changePassword(this.props.me._links.password.href, oldPassword, password)
        .then(result => {
          if (result.error) {
            this.setErrorState(result.error);
          } else {
            this.setSuccessfulState();
          }
        })
        .catch(err => {
          this.setErrorState(err);
        });
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
          children={t("password.changedSuccessfully")}
          onClose={() => this.onClose()}
        />
      );
    } else if (error) {
      message = <ErrorNotification error={error} />;
    }

    return (
      <form onSubmit={this.submit}>
        {message}
        <InputField
          label={t("password.currentPassword")}
          type="password"
          onChange={oldPassword =>
            this.setState({ ...this.state, oldPassword })
          }
          value={this.state.oldPassword ? this.state.oldPassword : ""}
          helpText={t("password.currentPasswordHelpText")}
        />
        <PasswordConfirmation
          passwordChanged={this.passwordChanged}
          key={this.state.passwordChanged ? "changed" : "unchanged"}
        />
        <SubmitButton
          disabled={!this.state.password}
          loading={loading}
          label={t("password.submit")}
        />
      </form>
    );
  }

  passwordChanged = (password: string) => {
    this.setState({ ...this.state, password });
  };

  onClose = () => {
    this.setState({
      ...this.state,
      passwordChanged: false
    });
  };
}

export default translate("commons")(ChangeUserPassword);
