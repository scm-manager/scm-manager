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

import { extensionPoints } from "@scm-manager/ui-extensions";
import { ComponentProps } from "react";

export type ProtocolLinkRendererProps<Protocol extends string | undefined = undefined> = ComponentProps<
  extensionPoints.MarkdownLinkProtocolRenderer<Protocol>["type"]["renderer"]
>;

/**
 * @deprecated use {@link MarkdownLinkProtocolRenderer}`["type"]` instead
 */
export type ProtocolLinkRendererExtension = extensionPoints.MarkdownLinkProtocolRenderer["type"];

export type ProtocolLinkRendererExtensionMap = {
  [protocol: string]: extensionPoints.MarkdownLinkProtocolRenderer["type"]["renderer"] | undefined;
};
