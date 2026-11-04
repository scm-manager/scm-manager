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

import React, { FC, ReactNode } from "react";
import { Page as CorePage } from "@scm-manager/ui-core";
import PageActions from "./PageActions";

type Props = {
  title?: ReactNode;
  // [DEPRECATED] Use useDocumentTitle hook inside the component instead.
  documentTitle?: string;
  afterTitle?: ReactNode;
  subtitle?: ReactNode;
  loading?: boolean;
  error?: Error | null;
  showContentOnError?: boolean;
  children: ReactNode;
};

const isPageActions = (node: ReactNode) =>
  React.isValidElement(node) && (node.type as { displayName?: string }).displayName === PageActions.displayName;

/**
 * @deprecated Please use `Page` from "@scm-manager/ui-core" and pass the actions with its `actions` prop
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const Page: FC<Props> = ({ documentTitle, children, ...props }) => {
  const childList = React.Children.toArray(children);
  return (
    <CorePage {...props} actions={childList.find(isPageActions)}>
      {childList.filter((child) => !isPageActions(child))}
    </CorePage>
  );
};

export default Page;
