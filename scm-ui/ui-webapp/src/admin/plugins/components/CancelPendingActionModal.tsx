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

import React, { FC, useEffect } from "react";
import PluginActionModal from "./PluginActionModal";
import { PendingPlugins } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { useCancelPendingPlugins } from "@scm-manager/ui-api";

type Props = {
  onClose: () => void;
  pendingPlugins: PendingPlugins;
};

const CancelPendingActionModal: FC<Props> = ({ onClose, pendingPlugins }) => {
  const [t] = useTranslation("admin");
  const { error, isLoading, update, isCancelled } = useCancelPendingPlugins();

  useEffect(() => {
    if (isCancelled) {
      onClose();
    }
  }, [onClose, isCancelled]);

  return (
    <PluginActionModal
      description={t("plugins.modal.cancelPending")}
      label={t("plugins.cancelPending")}
      onClose={onClose}
      pendingPlugins={pendingPlugins}
      success={isCancelled}
      loading={isLoading}
      error={error}
      execute={() => update(pendingPlugins)}
    />
  );
};

export default CancelPendingActionModal;
