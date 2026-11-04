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
import { useTranslation } from "react-i18next";
import { Icon } from "@scm-manager/ui-core";

const BooleanIcon: FC<{ value: boolean }> = ({ value }) => {
  const [t] = useTranslation("commons");
  return (
    <Icon
      className={value ? undefined : "has-text-secondary"}
      role="img"
      aria-hidden={false}
      aria-label={t(`booleanValue.${value}`)}
    >
      {value ? "check" : "times"}
    </Icon>
  );
};

export default BooleanIcon;
