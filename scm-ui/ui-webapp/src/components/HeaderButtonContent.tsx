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
import { Icon } from "@scm-manager/ui-components";

type Props = {
  burgerMode: boolean;
  label: string;
  icon: string;
};

export const headerButtonContentClassName =
  "is-flex is-align-items-center is-justify-content-flex-start has-text-inherit";

const HeaderButtonContent: FC<Props> = ({ burgerMode, icon, label }) => (
  <>
    <Icon name={icon} color="inherit" className={burgerMode ? "is-size-5" : "is-size-4"} />
    <span>{" " + label}</span>
  </>
);

export default HeaderButtonContent;
