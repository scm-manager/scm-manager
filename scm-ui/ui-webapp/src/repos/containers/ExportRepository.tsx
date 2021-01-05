/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FC, useState } from "react";
import { Button, Checkbox, Level, Notification, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Link, Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository;
};

const ExportRepository: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");
  const [compressed, setCompressed] = useState(true);
  const [loading, setLoading] = useState(false);

  const createExportLink = () => {
    let exportLink = (repository?._links.export as Link).href;
    if (compressed) {
      exportLink += "?compressed=true";
    }
    return exportLink;
  };

  if (!repository?._links?.export) {
    return null;
  }

  return (
    <>
      <hr />
      <Subtitle subtitle={t("export.subtitle")} />
      <>
        <Checkbox
          checked={compressed}
          label={t("export.compressed.label")}
          onChange={setCompressed}
          helpText={t("export.compressed.helpText")}
        />
        <Level
          right={
            <a color="primary" href={createExportLink()} onClick={() => setLoading(true)}>
              <Button color="primary" label={t("export.exportButton")} icon="file-export" />
            </a>
          }
        />
        {loading && <Notification onClose={() => setLoading(false)}>{t("export.exportStarted")}</Notification>}
      </>
    </>
  );
};
export default ExportRepository;
