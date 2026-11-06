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
import { Icon } from "@scm-manager/ui-core";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";

type Props = { file: FileDiff };

const FileTitle: FC<Props> = ({ file }) => {
  const [t] = useTranslation("repos");
  if (file.oldPath !== file.newPath && (file.type === "copy" || file.type === "rename")) {
    return (
      <>
        {file.oldPath}{" "}
        <Icon role="img" aria-hidden={false} aria-label={t("diff.renamedTo")}>
          arrow-right
        </Icon>{" "}
        {file.newPath}
      </>
    );
  } else if (file.type === "delete") {
    return <>{file.oldPath}</>;
  }
  return <>{file.newPath}</>;
};

export default FileTitle;
