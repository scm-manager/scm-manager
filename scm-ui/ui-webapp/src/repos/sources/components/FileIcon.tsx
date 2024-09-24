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
import { Icon } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  file: File;
};

const FileIcon: FC<Props> = ({ file }) => {
  const [t] = useTranslation("repos");
  if (file.subRepository) {
    return <Icon title={t("sources.fileTree.subRepository")} iconStyle="far" name="folder" color="inherit" />;
  } else if (file.directory) {
    return <Icon title={t("sources.fileTree.folder")} name="folder" color="inherit" />;
  }
  return <Icon title={t("sources.fileTree.file")} name="file" color="inherit" />;
};

export default FileIcon;
