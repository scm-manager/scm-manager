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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import InputField from "./InputField";

type BaseProps = {
  passwordChanged: (p1: string, p2: boolean) => void;
  passwordValidator?: (p: string) => boolean;
  onReturnPressed?: () => void;
  ref?: React.Ref<HTMLInputElement>;
};

const PasswordConfirmation: FC<BaseProps> = ({ passwordChanged, passwordValidator, onReturnPressed, ref }) => {
  const [t] = useTranslation("commons");
  const [password, setPassword] = useState("");
  const [confirmedPassword, setConfirmedPassword] = useState("");
  const [passwordValid, setPasswordValid] = useState(true);
  const [passwordConfirmationFailed, setPasswordConfirmationFailed] = useState(false);
  const isValid = passwordValid && !passwordConfirmationFailed;

  useEffect(() => passwordChanged(password, isValid), [password, isValid]);

  const validatePassword = (newPassword: string) => {
    if (passwordValidator) {
      return passwordValidator(newPassword);
    }

    return newPassword.length >= 6 && newPassword.length < 1024;
  };

  const handlePasswordValidationChange = (newConfirmedPassword: string) => {
    setConfirmedPassword(newConfirmedPassword);
    setPasswordConfirmationFailed(password !== newConfirmedPassword);
  };

  const handlePasswordChange = (newPassword: string) => {
    setPasswordConfirmationFailed(newPassword !== confirmedPassword);
    setPassword(newPassword);
    setPasswordValid(validatePassword(newPassword));
  };

  return (
    <div className="columns is-multiline">
      <div className="column is-half">
        <InputField
          label={t("password.newPassword")}
          type="password"
          onChange={handlePasswordChange}
          value={password}
          validationError={!passwordValid}
          errorMessage={t("password.passwordInvalid")}
          // @ts-ignore TODO check this
          ref={ref}
          onReturnPressed={onReturnPressed}
          testId="input-password"
        />
      </div>
      <div className="column is-half">
        <InputField
          label={t("password.confirmPassword")}
          type="password"
          onChange={handlePasswordValidationChange}
          value={confirmedPassword}
          validationError={passwordConfirmationFailed}
          errorMessage={t("password.passwordConfirmFailed")}
          onReturnPressed={onReturnPressed}
          testId="input-password-confirmation"
        />
      </div>
    </div>
  );
};

export default PasswordConfirmation;
