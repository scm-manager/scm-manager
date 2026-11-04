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
import { User } from "@scm-manager/ui-types";
import { useSetUserPassword } from "@scm-manager/ui-api";
import { Form, Subtitle, useDocumentTitle } from "@scm-manager/ui-core";
import { isPasswordValid } from "./userValidation";

type Props = {
  user: User;
};

type SetPasswordForm = {
  password: string;
  passwordConfirmation: string;
};

const createEmptyForm = (): SetPasswordForm => ({ password: "", passwordConfirmation: "" });

const SetUserPassword: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const [tCommons] = useTranslation("commons");
  useDocumentTitle(t("singleUser.menu.setPasswordNavLink"), user.displayName);
  const { setPassword } = useSetUserPassword(user);
  const [defaultValues, setDefaultValues] = useState(createEmptyForm);

  const submit = async ({ password }: SetPasswordForm) => {
    await setPassword(password);
    setDefaultValues(createEmptyForm());
  };

  return (
    <>
      <Subtitle subtitle={t("singleUserPassword.subtitle")} />
      <Form
        translationPath={["users", "singleUserPassword"]}
        defaultValues={defaultValues}
        onSubmit={submit}
        successMessageFallback={t("singleUserPassword.setPasswordSuccessful")}
      >
        <Form.Row>
          <Form.SecretConfirmation
            name="password"
            label={tCommons("password.newPassword")}
            confirmationLabel={tCommons("password.confirmPassword")}
            confirmationErrorMessage={tCommons("password.passwordConfirmFailed")}
            rules={{
              required: tCommons("password.passwordInvalid"),
              validate: (value: string) => isPasswordValid(value) || tCommons("password.passwordInvalid"),
            }}
          />
        </Form.Row>
      </Form>
    </>
  );
};

export default SetUserPassword;
