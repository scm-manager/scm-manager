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
import { Hit, HitProps, TextHitField } from "@scm-manager/ui-components";
import styled from "styled-components";

const LabelColumn = styled.th`
  min-width: 10rem;
`;

const GenericHit: FC<HitProps> = ({ hit }) => (
  <Hit>
    <Hit.Content>
      <table>
        {Object.keys(hit.fields).map(field => (
          <tr key={field}>
            <LabelColumn>{field}:</LabelColumn>
            <td>
              <TextHitField hit={hit} field={field} />
            </td>
          </tr>
        ))}
      </table>
    </Hit.Content>
  </Hit>
);

export default GenericHit;
