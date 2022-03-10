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
import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { User } from "@scm-manager/ui-types";
import {
  Checkbox,
  InputField,
  Level,
  PasswordConfirmation,
  SubmitButton,
  Subtitle,
  validation as validator
} from "@scm-manager/ui-components";
import * as userValidator from "./userValidation";

type Props = {
  submitForm: (p: User) => void;
  user?: User;
  loading?: boolean;
};

const UserForm: FC<Props> = ({ submitForm, user, loading }) => {
  const [t] = useTranslation("users");
  const [userState, setUserState] = useState<User>({
    name: "",
    displayName: "",
    mail: "",
    password: "",
    active: true,
    external: false,
    _links: {}
  });
  const [mailValidationError, setMailValidationError] = useState(false);
  const [displayNameValidationError, setDisplayNameValidationError] = useState(false);
  const [nameValidationError, setNameValidationError] = useState(false);
  const [passwordValid, setPasswordValid] = useState(false);

  useEffect(() => {
    if (user) {
      setUserState(user);
    }
  }, [user]);

  const createUserComponentsAreInvalid = () => {
    if (!user) {
      return nameValidationError || !userState.name || (!userState.external && !passwordValid);
    } else {
      return false;
    }
  };

  const editUserComponentsAreUnchanged = () => {
    if (user) {
      return (
        user.displayName === userState.displayName &&
        user.mail === userState.mail &&
        user.active === userState.active &&
        user.external === userState.external
      );
    } else {
      return false;
    }
  };

  const isInvalid = () => {
    return (
      createUserComponentsAreInvalid() ||
      editUserComponentsAreUnchanged() ||
      mailValidationError ||
      displayNameValidationError ||
      nameValidationError ||
      !userState.displayName
    );
  };

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!isInvalid()) {
      submitForm(userState);
    }
  };

  const passwordChangeField = (
    <PasswordConfirmation
      passwordChanged={(password, isPasswordValid) => {
        setPasswordValid(isPasswordValid);
        setUserState({ ...userState, password });
      }}
    />
  );
  let nameField = null;
  let subtitle = null;
  if (!user) {
    // create new user
    nameField = (
      <div className="column is-half">
        <InputField
          label={t("user.name")}
          onChange={name => {
            setNameValidationError(!!name && !validator.isNameValid(name));
            setUserState({ ...userState, name });
          }}
          value={userState ? userState.name : ""}
          validationError={nameValidationError}
          errorMessage={t("validation.name-invalid")}
          helpText={t("help.usernameHelpText")}
          testId="input-username"
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
      <form onSubmit={submit}>
        <div className="columns is-multiline">
          {nameField}
          <div className="column is-half">
            <InputField
              label={t("user.displayName")}
              onChange={displayName => {
                setDisplayNameValidationError(!userValidator.isDisplayNameValid(displayName));
                setUserState({ ...userState, displayName });
              }}
              value={userState ? userState.displayName : ""}
              validationError={displayNameValidationError}
              errorMessage={t("validation.displayname-invalid")}
              helpText={t("help.displayNameHelpText")}
              testId="input-displayname"
            />
          </div>
          <div className="column is-half">
            <InputField
              label={t("user.mail")}
              onChange={mail => {
                setMailValidationError(!!mail && !validator.isMailValid(mail));
                setUserState({ ...userState, mail });
              }}
              value={userState ? userState.mail : ""}
              validationError={mailValidationError}
              errorMessage={t("validation.mail-invalid")}
              helpText={t("help.mailHelpText")}
              testId="input-mail"
            />
          </div>
        </div>
        {!user && (
          <>
            <div className="columns">
              <div className="column">
                <Checkbox
                  label={t("user.externalFlag")}
                  onChange={external => setUserState({ ...userState, external })}
                  checked={userState.external}
                  helpText={t("help.externalFlagHelpText")}
                />
              </div>
            </div>
          </>
        )}
        {!userState.external && (
          <>
            {!user && passwordChangeField}
            <div className="columns">
              <div className="column">
                <Checkbox
                  label={t("user.active")}
                  onChange={active => setUserState({ ...userState, active })}
                  checked={userState ? userState.active : false}
                  helpText={t("help.activeHelpText")}
                />
              </div>
            </div>
          </>
        )}
        <Level right={<SubmitButton disabled={isInvalid()} loading={loading} label={t("userForm.button.submit")} />} />
      </form>
    </>
  );
};

export default UserForm;
