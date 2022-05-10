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
  ErrorNotification,
  Level,
  Notification,
  PasswordConfirmation,
  SubmitButton,
  Subtitle,
} from "@scm-manager/ui-components";
import { useSetUserPassword } from "@scm-manager/ui-api";

type Props = {
  user: User;
};

const SetUserPassword: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const { passwordOverwritten, setPassword, error, isLoading, reset } = useSetUserPassword(user);
  const [newPassword, setNewPassword] = useState("");
  const [passwordValid, setPasswordValid] = useState(false);

  useEffect(() => {
    if (passwordOverwritten) {
      setNewPassword("");
      setPasswordValid(false);
    }
  }, [passwordOverwritten]);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (newPassword) {
      setPassword(newPassword);
    }
  };

  const onPasswordChange = (newValue: string, valid: boolean) => {
    setNewPassword(newValue);
    setPasswordValid(!!newValue && valid);
  };

  let message;

  if (passwordOverwritten) {
    message = (
      <Notification type={"success"} children={t("singleUserPassword.setPasswordSuccessful")} onClose={reset} />
    );
  } else if (error) {
    message = <ErrorNotification error={error} />;
  }

  return (
    <form onSubmit={submit}>
      <Subtitle subtitle={t("singleUserPassword.subtitle")} />
      {message}
      <PasswordConfirmation passwordChanged={onPasswordChange} key={passwordOverwritten ? "changed" : "unchanged"} />
      <Level
        right={<SubmitButton disabled={!passwordValid} loading={isLoading} label={t("singleUserPassword.button")} />}
      />
    </form>
  );
};

export default SetUserPassword;
