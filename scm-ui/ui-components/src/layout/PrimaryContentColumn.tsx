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
import styled from "styled-components";
import { useSecondaryNavigation } from "../useSecondaryNavigation";

const PrimaryColumn = styled.div<{ collapsed: boolean }>`
  width: ${(props: { collapsed: boolean }) => (props.collapsed ? "89.7%" : "75%")};
  /* Render this column to full size if column construct breaks (page size too small). */
  @media (max-width: 785px) {
    width: 100%;
  }
`;

const PrimaryContentColumn: FC = ({ children }) => {
  const { collapsed } = useSecondaryNavigation();

  return (
    <PrimaryColumn className="column" collapsed={collapsed}>
      {children}
    </PrimaryColumn>
  );
};

export default PrimaryContentColumn;
