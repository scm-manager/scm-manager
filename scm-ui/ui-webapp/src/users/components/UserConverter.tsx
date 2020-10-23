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
import React, { FC, useState } from "react";
import {
  Button,
  Modal,
  PasswordConfirmation,
  SubmitButton,
  ErrorNotification,
  Level
} from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Link, User } from "@scm-manager/ui-types";
import { convertToExternal, convertToInternal } from "./convertUser";
import styled from "styled-components";

const ExternalDescription = styled.div`
  display: flex;
  align-items: center;
  font-weight: 400;
`;

type Props = {
  user: User;
  fetchUser: (user: User) => void;
};

const UserConverter: FC<Props> = ({ user, fetchUser }) => {
  const [t] = useTranslation("users");
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [password, setPassword] = useState("");
  const [passwordValid, setPasswordValid] = useState(false);
  const [error, setError] = useState<Error | undefined>();

  const toInternal = () => {
    convertToInternal((user._links.convertToInternal as Link).href, password)
      .then(() => fetchUser(user))
      .then(() => setShowPasswordModal(false))
      .catch(setError);
  };

  const toExternal = () => {
    convertToExternal((user._links.convertToExternal as Link).href)
      .then(() => fetchUser(user))
      .catch(setError);
  };

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
        />
      );
    } else {
      return <Button label={t("userForm.button.convertToExternal")} action={() => toExternal()} icon="exchange-alt" />;
    }
  };

  const passwordChangeField = <PasswordConfirmation passwordChanged={changePassword} />;
  const passwordModal = (
    <Modal
      body={passwordChangeField}
      closeFunction={() => setShowPasswordModal(false)}
      active={showPasswordModal}
      title={t("userForm.modal.passwordRequired")}
      footer={
        <SubmitButton
          action={() => password && passwordValid && toInternal()}
          disabled={!passwordValid}
          scrollToTop={false}
          label={t("userForm.modal.convertToInternal")}
        />
      }
    />
  );

  return (
    <div>
      {showPasswordModal && passwordModal}
      {error && <ErrorNotification error={error} />}
      <div className="columns is-multiline">
        <ExternalDescription className="column is-half">{getUserExternalDescription()}</ExternalDescription>
        <div className="column is-half">
          <Level right={getConvertButton()} />
        </div>
      </div>
    </div>
  );
};

export default UserConverter;
