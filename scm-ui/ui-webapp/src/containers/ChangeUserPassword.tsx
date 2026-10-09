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
import {
  ErrorNotification,
  InputField,
  Notification,
  PasswordConfirmation,
  Subtitle,
} from "@scm-manager/ui-components";
import { Button, Level, useDocumentTitle } from "@scm-manager/ui-core";
import { useChangeUserPassword, useRequiredMe } from "@scm-manager/ui-api";

const ChangeUserPassword: FC = () => {
  const [t] = useTranslation("commons");
  const me = useRequiredMe();
  useDocumentTitle(t("password.subtitle"), me.displayName);
  const { isPending, error, passwordChanged, changePassword, reset } = useChangeUserPassword(me);
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
    message = (
      <Notification type="success" onClose={reset}>
        {t("password.changedSuccessfully")}
      </Notification>
    );
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
          <Button
            type="submit"
            variant="primary"
            disabled={!passwordValid || !oldPassword}
            isLoading={isPending}
            testId="submit-button"
            onClick={() => window.scrollTo(0, 0)}
          >
            {t("password.submit")}
          </Button>
        }
      />
    </form>
  );
};

export default ChangeUserPassword;
