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
// @ts-ignore
import { Decoration } from "react-diff-view";
import styled from "styled-components";

const HunkDivider = styled.div`
  background: #98d8f3;
  @media screen and (max-width: 768px) {
    padding-left: 0px !important;
  } ;
`;

const HunkExpandDivider: FC = ({ children }) => {
  return (
    <Decoration>
      <HunkDivider className="is-size-7 pl-5">{children}</HunkDivider>
    </Decoration>
  );
};

export default HunkExpandDivider;
