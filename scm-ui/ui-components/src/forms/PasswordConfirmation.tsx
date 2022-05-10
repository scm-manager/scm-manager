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
import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import InputField from "./InputField";

type BaseProps = {
  passwordChanged: (p1: string, p2: boolean) => void;
  passwordValidator?: (p: string) => boolean;
  onReturnPressed?: () => void;
};

type InnerProps = BaseProps & {
  innerRef: React.Ref<HTMLInputElement>;
};

const PasswordConfirmation: FC<InnerProps> = ({ passwordChanged, passwordValidator, onReturnPressed, innerRef }) => {
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

    return newPassword.length >= 6 && newPassword.length < 32;
  };

  const handlePasswordValidationChange = (newConfirmedPassword: string) => {
    setConfirmedPassword(newConfirmedPassword);
    setPasswordConfirmationFailed(password !== newConfirmedPassword);
  };

  const handlePasswordChange = (event: React.ChangeEvent<HTMLInputElement> | string) => {
    let newPassword;
    if (typeof event === "string") {
      newPassword = event;
    } else {
      newPassword = event.target.value;
    }
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
          onChange={(event) => handlePasswordChange(event)}
          value={password}
          validationError={!passwordValid}
          errorMessage={t("password.passwordInvalid")}
          ref={innerRef}
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

export default React.forwardRef<HTMLInputElement, BaseProps>((props, ref) => (
  <PasswordConfirmation {...props} innerRef={ref} />
));
