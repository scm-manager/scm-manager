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

import { FileDiff } from "@scm-manager/ui-types";
import Tag from "../../Tag";
import classNames from "classnames";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";

type Props = { file: FileDiff };

const ChangeTag: FC<Props> = ({ file }) => {
  const [t] = useTranslation("repos");
  if (!file.type) {
    return null;
  }
  const key = "diff.changes." + file.type;
  let value = t(key);
  if (key === value) {
    value = file.type;
  }

  const color = value === "added" ? "success" : value === "deleted" ? "danger" : "info";
  return (
    <Tag
      className={classNames("has-text-weight-normal", "ml-3")}
      rounded={true}
      outlined={true}
      color={color}
      label={value}
    />
  );
};

export default ChangeTag;
