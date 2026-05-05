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
import { KeyboardIterator } from "@scm-manager/ui-core";
import { Plugin } from "@scm-manager/ui-types";
import PluginGroupEntry from "../components/PluginGroupEntry";
import { PluginModalContent } from "../containers/PluginsOverview";
import groupByCategory from "./groupByCategory";

type Props = {
  plugins: Plugin[];
  openModal: (content: PluginModalContent) => void;
};

const PluginList: FC<Props> = ({ plugins, openModal }) => {
  const groups = groupByCategory(plugins);
  return (
    <div className="content is-plugin-page">
      <KeyboardIterator>
        {groups.map((group) => {
          return <PluginGroupEntry group={group} openModal={openModal} key={group.name} />;
        })}
      </KeyboardIterator>
    </div>
  );
};

export default PluginList;
