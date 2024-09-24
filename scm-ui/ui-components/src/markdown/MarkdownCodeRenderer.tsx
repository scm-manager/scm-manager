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
import SyntaxHighlighter from "../SyntaxHighlighter";
import { ExtensionPoint, extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import { useIndexLinks } from "@scm-manager/ui-api";

type Props = {
  language?: string;
  value: string;
};

const MarkdownCodeRenderer: FC<Props> = props => {
  const binder = useBinder();
  const indexLinks = useIndexLinks();
  const { language } = props;

  const extensionProps = { ...props, indexLinks };
  const extensionKey = `markdown-renderer.code.${language}` as const;
  if (binder.hasExtension<extensionPoints.MarkdownCodeRenderer>(extensionKey, extensionProps)) {
    return <ExtensionPoint<extensionPoints.MarkdownCodeRenderer> name={extensionKey} props={extensionProps} />;
  }
  return <SyntaxHighlighter {...props} />;
};

export default MarkdownCodeRenderer;
