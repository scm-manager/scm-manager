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

import React, { FC } from "react";
import { Modal, Notification } from "@scm-manager/ui-components";
import { PendingPlugins } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import PendingPluginsQueue from "./PendingPluginsQueue";
import { Button } from "@scm-manager/ui-core";

type ModalBodyProps = {
  pendingPlugins: PendingPlugins;
};

const ModalBody: FC<ModalBodyProps> = ({ pendingPlugins }) => {
  const [t] = useTranslation("admin");
  return (
    <>
      <div className="media">
        <div className="content">
          <p>{t("plugins.modal.showPending")}</p>
          <PendingPluginsQueue pendingPlugins={pendingPlugins} />
        </div>
      </div>
      <div className="media">
        <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>
      </div>
    </>
  );
};

type Props = {
  onClose: () => void;
  pendingPlugins: PendingPlugins;
};

const ShowPendingModal: FC<Props> = ({ pendingPlugins, onClose }) => {
  const [t] = useTranslation("admin");
  return (
    <Modal
      title={t("plugins.showPending")}
      closeFunction={onClose}
      body={<ModalBody pendingPlugins={pendingPlugins} />}
      footer={<Button onClick={onClose}>{t("plugins.modal.close")}</Button>}
      active={true}
    />
  );
};

export default ShowPendingModal;
