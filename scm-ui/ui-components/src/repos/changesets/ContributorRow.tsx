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

const SizedTd = styled.td`
  width: 10rem;
`;

const ContributorRow: FC<{ label: string }> = ({ label, children }) => {
  return (
    <tr key={label}>
      <SizedTd>{label}:</SizedTd>
      <td className="is-ellipsis-overflow m-0">{children}</td>
    </tr>
  );
};

export default ContributorRow;
