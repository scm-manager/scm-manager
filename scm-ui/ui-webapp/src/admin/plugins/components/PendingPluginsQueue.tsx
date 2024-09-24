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
import { PendingPlugins } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";

type Props = {
  pendingPlugins: PendingPlugins;
};

type SectionProps = Props & {
  type: string;
  label: string;
};

const Section: FC<SectionProps> = ({ pendingPlugins, type, label }) => {
  const plugins = pendingPlugins?._embedded[type];
  if (!plugins || plugins.length === 0) {
    return null;
  }
  return (
    <>
      <strong>{label}</strong>
      <ul>
        {plugins.map(plugin => (
          <li key={plugin.name}>{plugin.name}</li>
        ))}
      </ul>
    </>
  );
};

const PendingPluginsQueue: FC<Props> = ({ pendingPlugins }) => {
  const [t] = useTranslation("admin");
  return (
    <>
      <Section pendingPlugins={pendingPlugins} type="new" label={t("plugins.modal.installQueue")} />
      <Section pendingPlugins={pendingPlugins} type="update" label={t("plugins.modal.updateQueue")} />
      <Section pendingPlugins={pendingPlugins} type="uninstall" label={t("plugins.modal.uninstallQueue")} />
    </>
  );
};

export default PendingPluginsQueue;
