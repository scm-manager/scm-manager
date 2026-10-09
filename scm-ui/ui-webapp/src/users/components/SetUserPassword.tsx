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
import { ErrorNotification, Notification, PasswordConfirmation, Subtitle } from "@scm-manager/ui-components";
import { useSetUserPassword } from "@scm-manager/ui-api";
import { Button, Level, useDocumentTitle } from "@scm-manager/ui-core";

type Props = {
  user: User;
};

const SetUserPassword: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  useDocumentTitle(t("singleUser.menu.setPasswordNavLink"), user.displayName);
  const { passwordOverwritten, setPassword, error, isPending, reset } = useSetUserPassword(user);
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
      <Notification type="success" onClose={reset}>
        {t("singleUserPassword.setPasswordSuccessful")}
      </Notification>
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
        right={
          <Button
            type="submit"
            variant="primary"
            disabled={!passwordValid}
            isLoading={isPending}
            testId="submit-button"
            onClick={() => window.scrollTo(0, 0)}
          >
            {t("singleUserPassword.button")}
          </Button>
        }
      />
    </form>
  );
};

export default SetUserPassword;
