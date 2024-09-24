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
import { Link } from "react-router-dom";
import { Tooltip } from "@scm-manager/ui-overlays";
import Icon from "../Icon";

const Button = styled(Link)`
  width: 50px;
  &:hover {
    color: var(--scm-info-color);
  }
`;

type Props = {
  link: string;
  tooltip: string;
  icon?: string;
  color?: string;
};

const JumpToFileButton: FC<Props> = ({ link, tooltip, icon, color }) => {
  const iconToUse = icon ?? "file-code";
  const colorToUse = color ?? "inherit";
  return (
    <Tooltip message={tooltip} side="top">
      <Button aria-label={tooltip} className="button is-clickable" to={link}>
        <Icon name={iconToUse} color={colorToUse} alt="" />
      </Button>
    </Tooltip>
  );
};

export default JumpToFileButton;
