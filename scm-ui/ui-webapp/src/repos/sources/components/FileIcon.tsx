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
import { File } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";

type Props = {
  file: File;
};

const FileIcon: FC<Props> = ({ file }) => {
  const [t] = useTranslation("repos");
  if (file.subRepository) {
    return (
      <Icon
        type="far"
        title={t("sources.fileTree.subRepository")}
        role="img"
        aria-hidden={false}
        aria-label={t("sources.fileTree.subRepository")}
      >
        folder
      </Icon>
    );
  } else if (file.directory) {
    return (
      <Icon
        title={t("sources.fileTree.folder")}
        role="img"
        aria-hidden={false}
        aria-label={t("sources.fileTree.folder")}
      >
        folder
      </Icon>
    );
  }
  return (
    <Icon title={t("sources.fileTree.file")} role="img" aria-hidden={false} aria-label={t("sources.fileTree.file")}>
      file
    </Icon>
  );
};

export default FileIcon;
