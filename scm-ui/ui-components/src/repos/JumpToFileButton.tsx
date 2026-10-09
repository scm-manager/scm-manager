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
import { Icon, IconLinkButton } from "@scm-manager/ui-core";

type Props = {
  link: string;
  tooltip: string;
  icon?: string;
  color?: string;
};

const JumpToFileButton: FC<Props> = ({ link, tooltip, icon, color }) => {
  const iconToUse = icon ?? "file-code";
  return (
    <IconLinkButton title={tooltip} aria-label={tooltip} to={link}>
      <Icon className={color ? `has-text-${color}` : undefined}>{iconToUse}</Icon>
    </IconLinkButton>
  );
};

export default JumpToFileButton;
