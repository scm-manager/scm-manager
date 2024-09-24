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
import { useTranslation } from "react-i18next";
import { PendingPlugins } from "@scm-manager/ui-types";
import { Notification } from "@scm-manager/ui-components";
import PluginActionModal from "./PluginActionModal";
import { useExecutePendingPlugins } from "@scm-manager/ui-api";

type Props = {
  onClose: () => void;
  pendingPlugins: PendingPlugins;
};

const ExecutePendingActionModal: FC<Props> = ({ onClose, pendingPlugins }) => {
  const [t] = useTranslation("admin");
  const { update, isLoading: loading, error, isExecuted } = useExecutePendingPlugins();

  return (
    <PluginActionModal
      description={t("plugins.modal.executePending")}
      label={t("plugins.modal.executeAndRestart")}
      onClose={onClose}
      pendingPlugins={pendingPlugins}
      error={error}
      loading={loading}
      success={isExecuted}
      execute={() => update(pendingPlugins)}
    >
      <Notification type="warning">{t("plugins.modal.restartNotification")}</Notification>
    </PluginActionModal>
  );
};

export default ExecutePendingActionModal;
