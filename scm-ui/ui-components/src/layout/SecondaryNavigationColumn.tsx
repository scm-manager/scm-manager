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
import { SecondaryNavigationProvider } from "../navigation/SecondaryNavigationContext";

const SecondaryColumn = styled.div<{ collapsed: boolean }>`
  flex: 0 0 auto;
  /* Navigation width should be as constant as possible. */
  width: ${(props: { collapsed: boolean }) => (props.collapsed ? "5.5rem" : "16rem")};
  /* Render this column to full size if column construct breaks (page size too small). */
  @media (max-width: 785px) {
    width: 100%;
  }
`;

const SecondaryNavigationColumnIntern: FC = ({ children }) => {
  const { collapsed } = useSecondaryNavigation();

  return (
    <SecondaryColumn className="column" collapsed={collapsed}>
      {children}
    </SecondaryColumn>
  );
};

const SecondaryNavigationColumn: FC = ({ children }) => {
  return (
    <SecondaryNavigationProvider>
      <SecondaryNavigationColumnIntern>{children}</SecondaryNavigationColumnIntern>
    </SecondaryNavigationProvider>
  );
};

export default SecondaryNavigationColumn;
