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
