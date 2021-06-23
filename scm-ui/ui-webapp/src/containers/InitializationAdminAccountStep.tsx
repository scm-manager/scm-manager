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
import { apiClient, validation, ErrorNotification, InputField, SubmitButton } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { useMutation } from "react-query";
import { isDisplayNameValid, isPasswordValid } from "../users/components/userValidation";
import { Links, Link } from "@scm-manager/ui-types";

const HeroSection = styled.section`
  padding-top: 2em;
`;

type Props = {
  data: { _links: Links };
};

type AdminAccountCreation = {
  startupToken: string;
  userName: string;
  displayName: string;
  email: string;
  password: string;
  passwordConfirmation: string;
};

const createAdmin = (link: string) => {
  return (data: AdminAccountCreation) => {
    return apiClient.post(link, data, "application/json").then(() => {
      return new Promise<void>((resolve) => resolve());
    });
  };
};

const useCreateAdmin = (link: string) => {
  const { mutate, isLoading, error, isSuccess } = useMutation<void, Error, AdminAccountCreation>(createAdmin(link));
  return {
    create: mutate,
    isLoading,
    error,
    isCreated: isSuccess,
  };
};

const InitializationAdminAccountStep: FC<Props> = ({ data }) => {
  const [t] = useTranslation("initialization");
  const [startupKey, setStartupKey] = useState("");
  const [userName, setUserName] = useState("scmadmin");
  const [displayName, setDisplayName] = useState("SCM Administrator");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirmation, setPasswordConfirmation] = useState("");
  const [userNameValidationError, setUserNameValidationError] = useState(false);
  const [displayNameValidationError, setDisplayNameValidationError] = useState(false);
  const [mailValidationError, setMailValidationError] = useState(false);
  const [passwordValidationError, setPasswordValidationError] = useState(false);
  const [passwordConfirmationValidationError, setPasswordConfirmationValidationError] = useState(false);

  const { create, isLoading, error, isCreated } = useCreateAdmin((data._links.initialAdminUser as Link).href);

  useEffect(() => {
    if (isCreated) {
      window.location.reload(false);
    }
  }, [isCreated]);

  const validateAndSetUserName = (newUserName: string) => {
    setUserNameValidationError(!validation.isNameValid(newUserName));
    setUserName(newUserName);
  };

  const validateAndSetDisplayName = (newDisplayName: string) => {
    setDisplayNameValidationError(!isDisplayNameValid(newDisplayName));
    setDisplayName(newDisplayName);
  };

  const validateAndSetEmail = (newEmail: string) => {
    setMailValidationError(!!newEmail && !validation.isMailValid(newEmail));
    setEmail(newEmail);
  };

  const validateAndSetPassword = (newPassword: string) => {
    setPasswordValidationError(!isPasswordValid(newPassword));
    setPasswordConfirmationValidationError(!!passwordConfirmation && passwordConfirmation !== newPassword);
    setPassword(newPassword);
  };

  const validateAndSetPasswordConfirmation = (newPasswordConfirmation: string) => {
    setPasswordConfirmationValidationError(newPasswordConfirmation !== password);
    setPasswordConfirmation(newPasswordConfirmation);
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    create({
      startupToken: startupKey,
      userName,
      displayName,
      email,
      password,
      passwordConfirmation,
    });
  };

  let errorComponent;
  if (error) {
    if (error.message === "Forbidden") {
      errorComponent = <ErrorNotification error={new Error(t("error.forbidden"))} />;
    } else {
      errorComponent = <ErrorNotification error={error} />;
    }
  }

  const formValid =
    !!(startupKey && userName && displayName && password && passwordConfirmation) &&
    !(
      userNameValidationError ||
      displayNameValidationError ||
      mailValidationError ||
      passwordValidationError ||
      passwordConfirmationValidationError
    );
  const component = (
    <div className="column is-8 box  has-background-white-ter">
      <form onSubmit={handleSubmit}>
        <h3 className="title">{t("title")}</h3>
        <h4 className="subtitle">{t("adminStep.title")}</h4>
        <p>{t("adminStep.description")}</p>
        <div className={"columns"}>
          <div className="column is-full-width">
            <InputField placeholder={t("adminStep.startupToken")} autofocus={true} onChange={setStartupKey} />
          </div>
        </div>
        <div className={"columns"}>
          <div className="column is-half">
            <InputField
              testId="username-input"
              label={t("adminStep.username")}
              onChange={validateAndSetUserName}
              validationError={userNameValidationError}
              value={userName}
            />
          </div>
          <div className="column is-half">
            <InputField
              testId="displayname-input"
              label={t("adminStep.displayname")}
              onChange={validateAndSetDisplayName}
              value={displayName}
              validationError={displayNameValidationError}
            />
          </div>
        </div>
        <div className={"columns"}>
          <div className="column is-full-width">
            <InputField
              label={t("adminStep.email")}
              onChange={validateAndSetEmail}
              value={email}
              validationError={mailValidationError}
            />
          </div>
        </div>
        <div className={"columns"}>
          <div className="column is-half">
            <InputField
              testId="password-input"
              label={t("adminStep.password")}
              type="password"
              onChange={validateAndSetPassword}
              validationError={passwordValidationError}
            />
          </div>
          <div className="column is-half">
            <InputField
              testId="password-confirmation-input"
              label={t("adminStep.password-confirmation")}
              type="password"
              onChange={validateAndSetPasswordConfirmation}
              validationError={passwordConfirmationValidationError}
            />
          </div>
        </div>
        {errorComponent}
        <div className={"columns"}>
          <div className="column is-full-width">
            <SubmitButton label={t("adminStep.submit")} fullWidth={true} loading={isLoading} disabled={!formValid} />
          </div>
        </div>
      </form>
    </div>
  );

  return (
    <HeroSection className="hero">
      <div className="hero-body">
        <div className="container">
          <div className="columns is-centered">{component}</div>
        </div>
      </div>
    </HeroSection>
  );
};

export default InitializationAdminAccountStep;
