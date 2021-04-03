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

import React, { FC, useState } from "react";
import { apiClient, ErrorNotification, InputField, SubmitButton } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { useRequiredIndexLink } from "@scm-manager/ui-api";
import { User, UserCreation } from "@scm-manager/ui-types";
import { useMutation, useQueryClient } from "react-query";

const HeroSection = styled.section`
  padding-top: 2em;
`;

type Props = {
  data: any;
};

type AdminAccountCreation = {
  initialPassword: string;
  userName: string;
  password: string;
  passwordConfirmation: string;
};

const createAdmin = (link: string) => {
  return (data: AdminAccountCreation) => {
    return apiClient
      .post(link, data, "application/json")
      .then(response => {
        const location = response.headers.get("Location");
        if (!location) {
          throw new Error("Server does not return required Location header");
        }
        return apiClient.get(location);
      })
      .then(response => response.json());
  };
};

const useAdminStep = (link: string) => {
  const { mutate, data, isLoading, error } = useMutation<AdminAccountCreation, Error, AdminAccountCreation>(
    createAdmin(link),
    {
      onSuccess: () => {
        return undefined;
      }
    }
  );
  return {
    create: (user: AdminAccountCreation) => mutate(user),
    isLoading,
    error,
    user: data
  };
};

const InitializationAdminAccountStep: FC<Props> = ({ data }) => {
  const [t] = useTranslation("initialization");
  const [startupKey, setStartupKey] = useState("");
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [passwordConfirmation, setPasswordConfirmation] = useState("");

  const { create, isLoading, error } = useAdminStep(data._links.initialAdminUser.href);

  const handleSubmit = () => {
    create({
      initialPassword: startupKey,
      userName,
      password,
      passwordConfirmation
    });
  };

  let component;
  if (error) {
    component = <ErrorNotification error={error} />;
  } else {
    component = (
      <div className="column is-8 box has-text-centered has-background-white-ter">
        <h3 className="title">{t("title")}</h3>
        <h4 className="subtitle">{t("adminStep.title")}</h4>
        <p>{t("adminStep.description")}</p>
        <form onSubmit={handleSubmit}>
          <InputField placeholder={t("adminStep.startupKey-placeholder")} autofocus={true} onChange={setStartupKey} />
          <InputField
            testId="username-input"
            placeholder={t("adminStep.username-placeholder")}
            onChange={setUserName}
          />
          <InputField
            testId="password-input"
            placeholder={t("adminStep.password-placeholder")}
            type="password"
            onChange={setPassword}
          />
          <InputField
            testId="password-confirmation-input"
            placeholder={t("adminStep.password-confirmation-placeholder")}
            type="password"
            onChange={setPasswordConfirmation}
          />
          <SubmitButton label={t("adminStep.submit")} fullWidth={true} loading={isLoading} />
        </form>
      </div>
    );
  }
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
