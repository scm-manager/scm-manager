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
import {
  ErrorNotification,
  InputField,
  Level,
  Notification,
  PasswordConfirmation,
  SubmitButton,
  Subtitle,
} from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Me } from "@scm-manager/ui-types";
import { useChangeUserPassword } from "@scm-manager/ui-api";

type Props = {
  me: Me;
};

const ChangeUserPassword: FC<Props> = ({ me }) => {
  const [t] = useTranslation("commons");
  const { isLoading, error, passwordChanged, changePassword, reset } = useChangeUserPassword(me);
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [passwordValid, setPasswordValid] = useState(false);

  useEffect(() => {
    if (passwordChanged) {
      setOldPassword("");
      setNewPassword("");
      setPasswordValid(false);
    }
  }, [passwordChanged]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (newPassword) {
      changePassword(oldPassword, newPassword);
    }
  };

  const onPasswordChange = (newValue: string, valid: boolean) => {
    setNewPassword(newValue);
    setPasswordValid(!!newValue && valid);
  };

  let message = null;

  if (passwordChanged) {
    message = <Notification type={"success"} children={t("password.changedSuccessfully")} onClose={reset} />;
  } else if (error) {
    message = <ErrorNotification error={error} />;
  }

  return (
    <form onSubmit={submit}>
      <Subtitle subtitle={t("password.subtitle")} />
      {message}
      <div className="columns">
        <div className="column">
          <InputField
            label={t("password.currentPassword")}
            type="password"
            onChange={setOldPassword}
            value={oldPassword}
            helpText={t("password.currentPasswordHelpText")}
          />
        </div>
      </div>
      <PasswordConfirmation passwordChanged={onPasswordChange} key={passwordChanged ? "changed" : "unchanged"} />
      <Level
        right={
          <SubmitButton disabled={!passwordValid || !oldPassword} loading={isLoading} label={t("password.submit")} />
        }
      />
    </form>
  );
};

export default ChangeUserPassword;
