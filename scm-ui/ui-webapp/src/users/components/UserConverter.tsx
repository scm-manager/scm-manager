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

import React, { FC, KeyboardEvent, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { User } from "@scm-manager/ui-types";
import { useConvertToExternal, useConvertToInternal } from "@scm-manager/ui-api";
import { Modal } from "@scm-manager/ui-components";
import { Button, ErrorNotification, Icon, InputField, Level } from "@scm-manager/ui-core";
import { isPasswordValid } from "./userValidation";

type Props = {
  user: User;
};

const UserConverter: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const [tCommons] = useTranslation("commons");
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [password, setPassword] = useState("");
  const [passwordConfirmation, setPasswordConfirmation] = useState("");
  const {
    isPending: isConvertingToInternal,
    error: convertingToInternalError,
    convertToInternal,
  } = useConvertToInternal();
  const {
    isPending: isConvertingToExternal,
    error: convertingToExternalError,
    convertToExternal,
  } = useConvertToExternal();
  const error = convertingToExternalError || convertingToInternalError || undefined;
  const isLoading = isConvertingToExternal || isConvertingToInternal;
  const initialFocusRef = useRef<HTMLInputElement>(null);
  const passwordValid = isPasswordValid(password);
  const passwordConfirmed = password === passwordConfirmation;
  const canConvertToInternal = passwordValid && passwordConfirmed && !isLoading;

  useEffect(() => setShowPasswordModal(false), [user]);

  const openPasswordModal = () => {
    setPassword("");
    setPasswordConfirmation("");
    setShowPasswordModal(true);
  };

  const submitPassword = () => {
    if (canConvertToInternal) {
      convertToInternal(user, password);
    }
  };

  const submitOnEnter = (event: KeyboardEvent<HTMLInputElement>) => {
    // an enter that confirms an input method composition must not convert the user
    if (event.key === "Enter" && !event.nativeEvent.isComposing) {
      event.preventDefault();
      submitPassword();
    }
  };

  const getUserExternalDescription = () => {
    if (user.external) {
      return t("userForm.userIsExternal");
    } else {
      return t("userForm.userIsInternal");
    }
  };

  const getConvertButton = () => {
    if (user.external) {
      return (
        <Button
          onClick={openPasswordModal}
          className="is-pulled-right"
          isLoading={isLoading}
          disabled={isLoading}
        >
          <Icon>exchange-alt</Icon>
          <span>{t("userForm.button.convertToInternal")}</span>
        </Button>
      );
    } else {
      return (
        <Button isLoading={isLoading} disabled={isLoading} onClick={() => convertToExternal(user)}>
          <Icon>exchange-alt</Icon>
          <span>{t("userForm.button.convertToExternal")}</span>
        </Button>
      );
    }
  };

  const passwordModal = (
    <Modal
      closeFunction={() => setShowPasswordModal(false)}
      active={showPasswordModal}
      title={t("userForm.modal.passwordRequired")}
      initialFocusRef={initialFocusRef}
      footer={
        <Button
          variant="primary"
          onClick={submitPassword}
          isLoading={isLoading}
          disabled={!canConvertToInternal}
          testId="submit-button"
        >
          {t("userForm.modal.convertToInternal")}
        </Button>
      }
    >
      <div className="columns is-multiline">
        <div className="column is-half">
          <InputField
            label={tCommons("password.newPassword")}
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            onKeyDown={submitOnEnter}
            error={password && !passwordValid ? tCommons("password.passwordInvalid") : undefined}
            ref={initialFocusRef}
            testId="input-password"
          />
        </div>
        <div className="column is-half">
          <InputField
            label={tCommons("password.confirmPassword")}
            type="password"
            value={passwordConfirmation}
            onChange={(event) => setPasswordConfirmation(event.target.value)}
            onKeyDown={submitOnEnter}
            error={passwordConfirmation && !passwordConfirmed ? tCommons("password.passwordConfirmFailed") : undefined}
            testId="input-password-confirmation"
          />
        </div>
      </div>
    </Modal>
  );

  return (
    <div>
      {showPasswordModal && passwordModal}
      {error && <ErrorNotification error={error} />}
      <div className="columns is-multiline">
        <div className={classNames("column", "is-half", "is-flex", "is-align-items-center")}>
          {getUserExternalDescription()}
        </div>
        <div className="column is-half">
          <Level right={getConvertButton()} />
        </div>
      </div>
    </div>
  );
};

export default UserConverter;
