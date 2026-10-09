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

import React, { FC, MouseEvent } from "react";
import { Icon, IconButton } from "@scm-manager/ui-core";

type Props = {
  icon: string;
  tooltip: string;
  onClick: () => void;
};

const DiffButton: FC<Props> = ({ icon, tooltip, onClick }) => {
  const handleClick = (e: MouseEvent) => {
    e.preventDefault();
    onClick();
  };

  return (
    <IconButton title={tooltip} aria-label={tooltip} onClick={handleClick}>
      <Icon>{icon}</Icon>
    </IconButton>
  );
};

export default DiffButton;
