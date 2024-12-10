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
import { useDocumentTitle } from "@scm-manager/ui-core";

type Props = {
  user: User;
};

const SetUserPassword: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  useDocumentTitle(t("singleUser.menu.setPasswordNavLink"), user.displayName);
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
