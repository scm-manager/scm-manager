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

import React, { FC, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { User } from "@scm-manager/ui-types";
import { useConvertToExternal, useConvertToInternal } from "@scm-manager/ui-api";
import {
  Button,
  ErrorNotification,
  Level,
  Modal,
  PasswordConfirmation,
  SubmitButton,
} from "@scm-manager/ui-components";

type Props = {
  user: User;
};

const UserConverter: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [password, setPassword] = useState("");
  const [passwordValid, setPasswordValid] = useState(false);
  const {
    isLoading: isConvertingToInternal,
    error: convertingToInternalError,
    convertToInternal,
  } = useConvertToInternal();
  const {
    isLoading: isConvertingToExternal,
    error: convertingToExternalError,
    convertToExternal,
  } = useConvertToExternal();
  const error = convertingToExternalError || convertingToInternalError || undefined;
  const isLoading = isConvertingToExternal || isConvertingToInternal;
  const initialFocusRef = useRef<HTMLInputElement>(null);

  useEffect(() => setShowPasswordModal(false), [user]);

  const changePassword = (password: string, valid: boolean) => {
    setPassword(password);
    setPasswordValid(valid);
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
          label={t("userForm.button.convertToInternal")}
          action={() => setShowPasswordModal(true)}
          icon="exchange-alt"
          className="is-pulled-right"
          loading={isLoading}
          disabled={isLoading}
        />
      );
    } else {
      return (
        <Button
          label={t("userForm.button.convertToExternal")}
          loading={isLoading}
          disabled={isLoading}
          action={() => convertToExternal(user)}
          icon="exchange-alt"
        />
      );
    }
  };

  const onReturnPressed = () => {
    if (password && passwordValid) {
      convertToInternal(user, password);
    }
  };

  const passwordModal = (
    <Modal
      closeFunction={() => setShowPasswordModal(false)}
      active={showPasswordModal}
      title={t("userForm.modal.passwordRequired")}
      initialFocusRef={initialFocusRef}
      footer={
        <SubmitButton
          action={() => password && passwordValid && convertToInternal(user, password)}
          loading={isLoading}
          disabled={!password || !passwordValid || isLoading}
          scrollToTop={false}
          label={t("userForm.modal.convertToInternal")}
        />
      }
    >
      <PasswordConfirmation passwordChanged={changePassword} onReturnPressed={onReturnPressed} ref={initialFocusRef} />
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
