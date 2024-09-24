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
import { useTranslation } from "react-i18next";
import { PluginCollection } from "@scm-manager/ui-types";
import PluginActionModal from "./PluginActionModal";
import { useUpdatePlugins } from "@scm-manager/ui-api";

type Props = {
  onClose: () => void;
  installedPlugins: PluginCollection;
};

const UpdateAllActionModal: FC<Props> = ({ installedPlugins, onClose }) => {
  const [t] = useTranslation("admin");
  const { update, isLoading, error, isUpdated } = useUpdatePlugins();

  useEffect(() => {
    if (isUpdated) {
      onClose();
    }
  }, [onClose, isUpdated]);

  return (
    <PluginActionModal
      description={t("plugins.modal.updateAll")}
      label={t("plugins.updateAll")}
      onClose={onClose}
      installedPlugins={installedPlugins}
      error={error}
      loading={isLoading}
      success={isUpdated}
      execute={() => update(installedPlugins)}
    />
  );
};
export default UpdateAllActionModal;
