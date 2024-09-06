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
import { devices } from "./devices";

export const DangerZone = styled.div`
  border: 1px solid #ff6a88;
  border-radius: 5px;

  > .level {
    flex-flow: wrap;

    .level-left {
      max-width: 100%;
    }

    .level-right {
      margin-top: 0.75rem;
    }
  }

  > *:not(:last-child) {
    padding-bottom: 1.5rem;
    border-bottom: solid 2px var(--scm-border-color);
  }
  @media screen and (max-width: ${devices.tablet.width}px) {
    .button {
      height: 100%;
      white-space: break-spaces;
    }
  }
`;

export default DangerZone;
