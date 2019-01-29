// @flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "@scm-manager/ui-types";
import {
  Subtitle,
  Checkbox,
  InputField,
  PasswordConfirmation,
  SubmitButton,
  validation as validator
} from "@scm-manager/ui-components";
import * as userValidator from "./userValidation";

type Props = {
  submitForm: User => void,
  user?: User,
  loading?: boolean,
  t: string => string
};

type State = {
  user: User,
  mailValidationError: boolean,
  nameValidationError: boolean,
  displayNameValidationError: boolean,
  passwordValid: boolean
};

class UserForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      user: {
        name: "",
        displayName: "",
        mail: "",
        password: "",
        admin: false,
        active: true,
        _links: {}
      },
      mailValidationError: false,
      displayNameValidationError: false,
      nameValidationError: false,
      passwordValid: false
    };
  }

  componentDidMount() {
    const { user } = this.props;
    if (user) {
      this.setState({ user: { ...user } });
    }
  }

  isFalsy(value) {
    if (!value) {
      return true;
    }
    return false;
  }

  isValid = () => {
    const user = this.state.user;
    return !(
      this.state.nameValidationError ||
      this.state.mailValidationError ||
      this.state.displayNameValidationError ||
      this.isFalsy(user.name) ||
      this.isFalsy(user.displayName) ||
      this.isFalsy(user.mail) ||
      !this.state.passwordValid
    );
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (this.isValid()) {
      this.props.submitForm(this.state.user);
    }
  };

  render() {
    const { loading, t } = this.props;
    const user = this.state.user;

    let nameField = null;
    let passwordChangeField = null;
    let subtitle = null;
    if (!this.props.user) {
      // create new user
      nameField = (
        <div className="column is-half">
          <InputField
            label={t("user.name")}
            onChange={this.handleUsernameChange}
            value={user ? user.name : ""}
            validationError={this.state.nameValidationError}
            errorMessage={t("validation.name-invalid")}
            helpText={t("help.usernameHelpText")}
          />
        </div>
      );

      passwordChangeField = (
        <PasswordConfirmation passwordChanged={this.handlePasswordChange} />
      );
    } else {
      // edit existing user
      subtitle = <Subtitle subtitle={t("userForm.subtitle")} />;
    }
    return (
      <>
        {subtitle}
        <form onSubmit={this.submit}>
          <div className="columns">
            <div className="column is-half">
              {nameField}
              <InputField
                label={t("user.displayName")}
                onChange={this.handleDisplayNameChange}
                value={user ? user.displayName : ""}
                validationError={this.state.displayNameValidationError}
                errorMessage={t("validation.displayname-invalid")}
                helpText={t("help.displayNameHelpText")}
              />
            </div>
            <div className="column is-half">
              <InputField
                label={t("user.mail")}
                onChange={this.handleEmailChange}
                value={user ? user.mail : ""}
                validationError={this.state.mailValidationError}
                errorMessage={t("validation.mail-invalid")}
                helpText={t("help.mailHelpText")}
              />
            </div>
          </div>
          <div className="columns">
            <div className="column">
              {passwordChangeField}
              <Checkbox
                label={t("user.admin")}
                onChange={this.handleAdminChange}
                checked={user ? user.admin : false}
                helpText={t("help.adminHelpText")}
              />
              <Checkbox
                label={t("user.active")}
                onChange={this.handleActiveChange}
                checked={user ? user.active : false}
                helpText={t("help.activeHelpText")}
              />
            </div>
          </div>
          <div className="columns">
            <div className="column">
              <SubmitButton
                disabled={!this.isValid()}
                loading={loading}
                label={t("userForm.button")}
              />
            </div>
          </div>
        </form>
      </>
    );
  }

  handleUsernameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isNameValid(name),
      user: { ...this.state.user, name }
    });
  };

  handleDisplayNameChange = (displayName: string) => {
    this.setState({
      displayNameValidationError: !userValidator.isDisplayNameValid(
        displayName
      ),
      user: { ...this.state.user, displayName }
    });
  };

  handleEmailChange = (mail: string) => {
    this.setState({
      mailValidationError: !validator.isMailValid(mail),
      user: { ...this.state.user, mail }
    });
  };

  handlePasswordChange = (password: string, passwordValid: boolean) => {
    this.setState({
      user: { ...this.state.user, password },
      passwordValid: !this.isFalsy(password) && passwordValid
    });
  };

  handleAdminChange = (admin: boolean) => {
    this.setState({ user: { ...this.state.user, admin } });
  };

  handleActiveChange = (active: boolean) => {
    this.setState({ user: { ...this.state.user, active } });
  };
}

export default translate("users")(UserForm);
