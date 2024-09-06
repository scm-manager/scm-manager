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
import { CardList, Collapsible } from "@scm-manager/ui-layout";

type Props = {
  namespaceHeader: ReactNode;
  elements: ReactNode[];
  collapsed?: boolean;
  onCollapsedChange?: (collapsed: boolean) => void;
};

const GroupEntries: FC<Props> = ({ namespaceHeader, elements, collapsed, onCollapsedChange }) => (
  <Collapsible className="mb-5" header={namespaceHeader} collapsed={collapsed} onCollapsedChange={onCollapsedChange}>
    <CardList>{elements}</CardList>
  </Collapsible>
);

export default GroupEntries;
