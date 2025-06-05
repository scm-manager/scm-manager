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

import { HalRepresentation, HalRepresentationWithEmbedded } from "./hal";

export type PluginSet = HalRepresentation & {
  id: string;
  name: string;
  sequence: number;
  features: string[];
  plugins: Plugin[];
  images: Record<string, string>;
};

export type Plugin = HalRepresentation & {
  name: string;
  version: string;
  newVersion?: string;
  displayName: string;
  description?: string;
  author: string;
  category: string;
  avatarUrl?: string;
  pending: boolean;
  markedForUninstall?: boolean;
  dependencies: string[];
  optionalDependencies: string[];
};

export type PluginCenterStatus = "OK" | "ERROR" | "DEACTIVATED";

export type PluginCollection = HalRepresentationWithEmbedded<{
  plugins: Plugin[];
}> & { pluginCenterStatus: PluginCenterStatus };

export const isPluginCollection = (input: HalRepresentation): input is PluginCollection =>
  input._embedded ? "plugins" in input._embedded : false;

export type PluginGroup = {
  name: string;
  plugins: Plugin[];
};

export type PendingPlugins = HalRepresentationWithEmbedded<{
  new: Plugin[];
  update: Plugin[];
  uninstall: Plugin[];
}>;
