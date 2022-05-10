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
  ErrorNotification,
  Icon,
  Modal,
  NoStyleButton,
  SmallLoadingSpinner,
  Tooltip,
} from "@scm-manager/ui-components";
import { PluginCenterAuthenticationInfo } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";

type PluginCenterAuthInfoProps = {
  data?: PluginCenterAuthenticationInfo;
  isLoading: boolean;
  error: Error | null;
};

const PluginCenterAuthInfo: FC<PluginCenterAuthInfoProps> = (props) => (
  <div className="ml-3 is-flex is-align-items-center is-size-5">
    <Inner {...props} />
  </div>
);

const Inner: FC<PluginCenterAuthInfoProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <SmallLoadingSpinner />;
  }
  if (error) {
    return <AuthenticationError error={error} />;
  }
  if (!data || !data.pluginCenterSubject) {
    return null;
  }
  if (data.failed) {
    return <AuthenticationFailed info={data} />;
  }

  return <Authenticated info={data} />;
};

type ErrorProps = {
  error: Error;
};

const AuthenticationError: FC<ErrorProps> = ({ error }) => {
  const [t] = useTranslation("admin");
  const [showModal, setShowModal] = useState(false);
  return (
    <>
      <Tooltip message={t("plugins.myCloudogu.error.info")} multiline={true}>
        <NoStyleButton onClick={() => setShowModal(true)}>
          <Icon name="exclamation-triangle" color="danger" className="is-size-5" />
        </NoStyleButton>
      </Tooltip>
      {showModal ? <ErrorModal error={error} onClose={() => setShowModal(false)} /> : null}
    </>
  );
};

type ErrorModalProps = {
  error: Error;
  onClose: () => void;
};

const ErrorModal: FC<ErrorModalProps> = ({ error, onClose }) => {
  const [t] = useTranslation("admin");
  return (
    <Modal title={t("plugins.myCloudogu.error.title")} closeFunction={onClose} active={true}>
      <ErrorNotification error={error} />
    </Modal>
  );
};

type InfoProps = {
  info: PluginCenterAuthenticationInfo;
};

const AuthenticationFailed: FC<InfoProps> = ({ info }) => {
  const [t] = useTranslation("admin");
  return (
    <Tooltip
      message={t("plugins.myCloudogu.failed.info", {
        pluginCenterSubject: info.pluginCenterSubject,
      })}
      multiline={true}
    >
      <Icon name="exclamation-triangle" color="danger" />
    </Tooltip>
  );
};

const Authenticated: FC<InfoProps> = ({ info }) => {
  const [t] = useTranslation("admin");
  return (
    <Tooltip
      message={t("plugins.myCloudogu.connectionInfo", {
        pluginCenterSubject: info.pluginCenterSubject,
      })}
      multiline={true}
    >
      <Icon name="check-circle" color="info" />
    </Tooltip>
  );
};

export default PluginCenterAuthInfo;
