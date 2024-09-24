/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC, useEffect } from "react";
import { apiClient, validation, ErrorNotification, InputField, SubmitButton } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { useMutation } from "react-query";
import { isDisplayNameValid, isPasswordValid } from "../users/components/userValidation";
import { Links, Link } from "@scm-manager/ui-types";
import { useForm } from "react-hook-form";

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

const createAdmin = (link: string) => (data: AdminAccountCreation) => apiClient.post(link, data, "application/json");

const useCreateAdmin = (link: string) => {
  const { mutate, isLoading, error, isSuccess } = useMutation<unknown, Error, AdminAccountCreation>(createAdmin(link));
  return {
    create: mutate,
    isLoading,
    error,
    isCreated: isSuccess
  };
};

const InitializationAdminAccountStep: FC<Props> = ({ data }) => {
  const [t] = useTranslation("initialization");
  const { formState, register, handleSubmit, getValues, setError, clearErrors } = useForm<AdminAccountCreation>({
    defaultValues: {
      userName: "scmadmin",
      displayName: "SCM Administrator",
      email: "",
      password: "",
      passwordConfirmation: ""
    },
    mode: "onChange"
  });

  const { create, isLoading, error, isCreated } = useCreateAdmin((data._links.initialAdminUser as Link).href);

  useEffect(() => {
    if (isCreated) {
      window.location.reload();
    }
  }, [isCreated]);

  const validateUserName = (newUserName: string) => {
    return validation.isNameValid(newUserName);
  };

  const validateDisplayName = (newDisplayName: string) => {
    return isDisplayNameValid(newDisplayName);
  };

  const validateEmail = (newEmail: string) => {
    return !newEmail || validation.isMailValid(newEmail);
  };

  const validatePassword = (newPassword: string) => {
    if (getValues("passwordConfirmation") !== newPassword) {
      setError("passwordConfirmation", { type: "manual", message: "does not match password" });
    } else {
      clearErrors("passwordConfirmation");
    }
    return isPasswordValid(newPassword);
  };

  const validatePasswordConfirmation = (newPasswordConfirmation: string) => {
    return newPasswordConfirmation === getValues("password");
  };

  const onSubmit = (admin: AdminAccountCreation) => {
    create(admin);
  };

  let errorComponent;
  if (error) {
    if (error.message === "Forbidden") {
      errorComponent = <ErrorNotification error={new Error(t("error.forbidden"))} />;
    } else {
      errorComponent = <ErrorNotification error={error} />;
    }
  }

  const component = (
    <div className="column is-8 box  has-background-secondary-less">
      <form onSubmit={handleSubmit(onSubmit)}>
        <h3 className="title">{t("title")}</h3>
        <h4 className="subtitle">{t("adminStep.title")}</h4>
        <p>{t("adminStep.description")}</p>
        <div className="columns">
          <div className="column is-full-width">
            <InputField placeholder={t("adminStep.startupToken")} autofocus={true} {...register("startupToken")} />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              testId="username-input"
              label={t("adminStep.username")}
              validationError={!!formState.errors.userName}
              {...register("userName", { validate: validateUserName })}
            />
          </div>
          <div className="column is-half">
            <InputField
              testId="displayname-input"
              label={t("adminStep.displayname")}
              validationError={!!formState.errors.displayName}
              {...register("displayName", { validate: validateDisplayName })}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-full-width">
            <InputField
              label={t("adminStep.email")}
              validationError={!!formState.errors.email}
              {...register("email", { validate: validateEmail })}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            <InputField
              testId="password-input"
              label={t("adminStep.password")}
              type="password"
              validationError={!!formState.errors.password}
              {...register("password", { validate: validatePassword })}
            />
          </div>
          <div className="column is-half">
            <InputField
              testId="password-confirmation-input"
              label={t("adminStep.password-confirmation")}
              type="password"
              validationError={!!formState.errors.passwordConfirmation}
              {...register("passwordConfirmation", { validate: validatePasswordConfirmation })}
            />
          </div>
        </div>
        {errorComponent}
        <div className="columns">
          <div className="column is-full-width">
            <SubmitButton
              label={t("adminStep.submit")}
              fullWidth={true}
              loading={isLoading}
              disabled={!formState.isValid}
            />
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
