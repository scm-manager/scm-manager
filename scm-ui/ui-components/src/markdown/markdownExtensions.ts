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
import { ComponentProps, FC, ReactNode } from "react";

type WithChildren<T> = T extends FC<infer P> ? FC<P & { children?: ReactNode }> : never;

export type ProtocolLinkRendererProps<Protocol extends string | undefined = undefined> = ComponentProps<
  WithChildren<extensionPoints.MarkdownLinkProtocolRenderer<Protocol>["type"]["renderer"]>
>;

/**
 * @deprecated use {@link MarkdownLinkProtocolRenderer}`["type"]` instead
 */
export type ProtocolLinkRendererExtension = WithChildren<extensionPoints.MarkdownLinkProtocolRenderer["type"]>;

export type ProtocolLinkRendererExtensionMap = {
  [protocol: string]: WithChildren<extensionPoints.MarkdownLinkProtocolRenderer["type"]["renderer"]> | undefined;
};
