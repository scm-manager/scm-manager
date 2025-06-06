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
import { CardColumnGroup } from "@scm-manager/ui-components";
import { PluginCenterAuthenticationInfo, PluginGroup } from "@scm-manager/ui-types";
import PluginEntry from "./PluginEntry";
import { PluginModalContent } from "../containers/PluginsOverview";

type Props = {
  group: PluginGroup;
  openModal: (content: PluginModalContent) => void;
  pluginCenterAuthInfo?: PluginCenterAuthenticationInfo;
};

const PluginGroupEntry: FC<Props> = ({ openModal, group, pluginCenterAuthInfo }) => {
  const entries = group.plugins.map(plugin => {
    return (
      <PluginEntry
        plugin={plugin}
        openModal={openModal}
        key={plugin.name}
        pluginCenterAuthInfo={pluginCenterAuthInfo}
      />
    );
  });
  return <CardColumnGroup name={group.name} elements={entries} />;
};

export default PluginGroupEntry;
