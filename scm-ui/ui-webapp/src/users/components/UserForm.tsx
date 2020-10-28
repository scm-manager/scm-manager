/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Link, User } from "@scm-manager/ui-types";
import {
  Checkbox,
  ErrorNotification,
  InputField,
  Level,
  PasswordConfirmation,
  SubmitButton,
  Subtitle,
  validation as validator
} from "@scm-manager/ui-components";
import * as userValidator from "./userValidation";

type Props = WithTranslation & {
  submitForm: (p: User) => void;
  user?: User;
  loading?: boolean;
};

type State = {
  user: User;
  mailValidationError: boolean;
  nameValidationError: boolean;
  displayNameValidationError: boolean;
  passwordValid: boolean;
  error?: Error;
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
        active: true,
        external: false,
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
      this.setState({
        user: {
          ...user
        }
      });
    }
  }

  createUserComponentsAreInvalid = () => {
    const user = this.state.user;
    if (!this.props.user) {
      return this.state.nameValidationError || !user.name || (!user.external && !this.state.passwordValid);
    } else {
      return false;
    }
  };

  editUserComponentsAreUnchanged = () => {
    const user = this.state.user;
    if (this.props.user) {
      return (
        this.props.user.displayName === user.displayName &&
        this.props.user.mail === user.mail &&
        this.props.user.active === user.active &&
        this.props.user.external === user.external
      );
    } else {
      return false;
    }
  };

  isInvalid = () => {
    const { user } = this.state;

    return (
      this.createUserComponentsAreInvalid() ||
      this.editUserComponentsAreUnchanged() ||
      this.state.mailValidationError ||
      this.state.displayNameValidationError ||
      this.state.nameValidationError ||
      !user.displayName
    );
  };

  submit = (event: Event) => {
    event.preventDefault();
    if (!this.isInvalid()) {
      this.props.submitForm(this.state.user);
    }
  };

  render() {
    const { loading, t } = this.props;
    const { user, error } = this.state;

    const passwordChangeField = <PasswordConfirmation passwordChanged={this.handlePasswordChange} />;
    let nameField = null;
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
    } else {
      // edit existing user
      subtitle = <Subtitle subtitle={t("userForm.subtitle")} />;
    }

    return (
      <>
        {subtitle}
        <form onSubmit={this.submit}>
          <div className="columns is-multiline">
            {nameField}
            <div className="column is-half">
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
          {!this.props.user && (
            <>
              <div className="columns">
                <div className="column">
                  <Checkbox
                    label={t("user.externalFlag")}
                    onChange={this.handleExternalChange}
                    checked={user.external}
                    helpText={t("help.externalFlagHelpText")}
                  />
                </div>
              </div>
            </>
          )}
          {!user.external && (
            <>
              {!this.props.user && passwordChangeField}
              <div className="columns">
                <div className="column">
                  <Checkbox
                    label={t("user.active")}
                    onChange={this.handleActiveChange}
                    checked={user ? user.active : false}
                    helpText={t("help.activeHelpText")}
                  />
                </div>
              </div>
            </>
          )}
          {error && <ErrorNotification error={error} />}
          <Level right={<SubmitButton disabled={this.isInvalid()} loading={loading} label={t("userForm.button.submit")} />} />
        </form>
      </>
    );
  }

  handleUsernameChange = (name: string) => {
    this.setState({
      nameValidationError: !validator.isNameValid(name),
      user: {
        ...this.state.user,
        name
      }
    });
  };

  handleDisplayNameChange = (displayName: string) => {
    this.setState({
      displayNameValidationError: !userValidator.isDisplayNameValid(displayName),
      user: {
        ...this.state.user,
        displayName
      }
    });
  };

  handleEmailChange = (mail: string) => {
    this.setState({
      mailValidationError: !!mail && !validator.isMailValid(mail),
      user: {
        ...this.state.user,
        mail
      }
    });
  };

  handlePasswordChange = (password: string, passwordValid: boolean) => {
    this.setState({
      user: {
        ...this.state.user,
        password
      },
      passwordValid: !!password && passwordValid
    });
  };

  handleActiveChange = (active: boolean) => {
    this.setState({
      user: {
        ...this.state.user,
        active
      }
    });
  };

  handleExternalChange = (external: boolean) => {
    this.setState({
      user: {
        ...this.state.user,
        external
      }
    });
  };
}

export default withTranslation("users")(UserForm);
