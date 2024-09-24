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
import { File, Link } from "@scm-manager/ui-types";
import { Notification } from "@scm-manager/ui-components";
import { Trans, useTranslation } from "react-i18next";

type Props = {
  src: string | File;
  download?: string | File;
  height?: string;
};

const createHref = (src: string | File): string => {
  if (typeof src === "string") {
    return src;
  }
  return (src._links.self as Link).href;
};

const PdfViewer: FC<Props> = ({ src, download, height = "50rem" }) => {
  const [t] = useTranslation("commons");
  const href = createHref(src);
  const downloadHref = download ? createHref(download) : href;
  return (
    <div style={{ height }}>
      <object height="100%" width="100%" type="application/pdf" data={href + "#toolbar=0&navpanes=0&scrollbar=0"}>
        <Notification type="warning">
          <Trans t={t} i18nKey="pdfViewer.error">
            Failed to display the document. Please download it from <a href={downloadHref}>here</a>.
          </Trans>
        </Notification>
      </object>
    </div>
  );
};

export default PdfViewer;
