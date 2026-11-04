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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Form, Subtitle, useDocumentTitle } from "@scm-manager/ui-core";
import { useChangeUserPassword, useRequiredMe } from "@scm-manager/ui-api";
import { isPasswordValid } from "../users/components/userValidation";

type ChangePasswordForm = {
  oldPassword: string;
  password: string;
  passwordConfirmation: string;
};

const createEmptyForm = (): ChangePasswordForm => ({ oldPassword: "", password: "", passwordConfirmation: "" });

const ChangeUserPassword: FC = () => {
  const [t] = useTranslation("commons");
  const me = useRequiredMe();
  useDocumentTitle(t("password.subtitle"), me.displayName);
  const { changePassword } = useChangeUserPassword(me);
  // new default values reset the form, so the passwords are cleared after a successful change
  const [defaultValues, setDefaultValues] = useState(createEmptyForm);

  const submit = async ({ oldPassword, password }: ChangePasswordForm) => {
    await changePassword(oldPassword, password);
    setDefaultValues(createEmptyForm());
  };

  return (
    <>
      <Subtitle subtitle={t("password.subtitle")} />
      <Form
        translationPath={["commons", "password"]}
        defaultValues={defaultValues}
        onSubmit={submit}
        successMessageFallback={t("password.changedSuccessfully")}
      >
        <Form.Row>
          <Form.Input
            name="oldPassword"
            type="password"
            label={t("password.currentPassword")}
            rules={{ required: true }}
          />
        </Form.Row>
        <Form.Row>
          <Form.SecretConfirmation
            name="password"
            label={t("password.newPassword")}
            confirmationLabel={t("password.confirmPassword")}
            confirmationErrorMessage={t("password.passwordConfirmFailed")}
            rules={{
              required: t("password.passwordInvalid"),
              validate: (value: string) => isPasswordValid(value) || t("password.passwordInvalid"),
            }}
          />
        </Form.Row>
      </Form>
    </>
  );
};

export default ChangeUserPassword;
