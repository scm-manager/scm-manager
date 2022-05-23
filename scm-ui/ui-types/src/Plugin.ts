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

import { HalRepresentation, HalRepresentationWithEmbedded } from "./hal";

type PluginType = "SCM" | "CLOUDOGU";

export type PluginSet = HalRepresentation & {
  id: string;
  name: string;
  sequence: number;
  features: string[];
  plugins: string[];
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
  type: PluginType;
  markedForUninstall?: boolean;
  dependencies: string[];
  optionalDependencies: string[];
};

export type PluginCollection = HalRepresentationWithEmbedded<{
  plugins: Plugin[];
}>;

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

export type PluginCenterAuthenticationInfo = HalRepresentation & {
  principal?: string;
  pluginCenterSubject?: string;
  date?: string;
  default: boolean;
  failed: boolean;
};
