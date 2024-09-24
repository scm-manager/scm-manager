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
import {
  ErrorNotification,
  InputField,
  Level,
  Notification,
  PasswordConfirmation,
  SubmitButton,
  Subtitle
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
