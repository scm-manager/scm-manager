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
      <Tooltip message={t("plugins.cloudoguPlatform.error.info")} multiline={true}>
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
    <Modal title={t("plugins.cloudoguPlatform.error.title")} closeFunction={onClose} active={true}>
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
      message={t("plugins.cloudoguPlatform.failed.info", {
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
      message={t("plugins.cloudoguPlatform.connectionInfo", {
        pluginCenterSubject: info.pluginCenterSubject,
      })}
      multiline={true}
    >
      <Icon name="check-circle" color="info" />
    </Tooltip>
  );
};

export default PluginCenterAuthInfo;
