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

import styled from "styled-components";
import { devices } from "@scm-manager/ui-components";

const HeaderButton = styled.div`
  @media screen and (max-width: ${devices.desktop.width - 1}px) {
    border-top: 1px solid var(--scm-white-color);
    margin-top: 1rem;
    padding-top: 1rem;
    padding-bottom: 1rem;
  }

  @media screen and (min-width: ${devices.desktop.width}px) {
    margin-left: 2rem;
  }
`;

export default HeaderButton;
