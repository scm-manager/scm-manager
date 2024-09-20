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

import React, { FC } from "react";
import { Trans, useTranslation } from "react-i18next";
import { Statistics } from "@scm-manager/ui-types";
import { Tag } from "@scm-manager/ui-components";
import styled from "styled-components";

type DiffStatisticsProps = { data: Statistics | undefined };

const DiffStatisticsContainer = styled.div`
  float: left;
  height: 40px;
  display: flex;
  align-items: center;
`;

const DiffStatistics: FC<DiffStatisticsProps> = ({ data }) => {
  const [t] = useTranslation("repos");
  return !data ? (
    <div></div>
  ) : (
    <DiffStatisticsContainer>
      <Trans
        t={t}
        i18nKey="changesets.showModifiedFiles"
        values={{ newFiles: data.added, modified: data.modified, deleted: data.deleted }}
        components={{ tag: <Tag size={"normal"} rounded={true} className={"mx-1"} /> }}
      ></Trans>
      {data.renamed > 0 && (
        <Trans
          t={t}
          i18nKey="changesets.showRenamedFiles"
          values={{ renamed: data.renamed }}
          components={{ tag: <Tag size={"normal"} rounded={true} className={"mx-1"} /> }}
        ></Trans>
      )}
      {data.copied > 0 && (
        <Trans
          t={t}
          i18nKey="changesets.showCopiedFiles"
          values={{ copied: data.copied }}
          components={{ tag: <Tag size={"normal"} rounded={true} className={"mx-1"} /> }}
        ></Trans>
      )}
      <span className="ml-1">{t("changesets.showFiles")}</span>
    </DiffStatisticsContainer>
  );
};

export default DiffStatistics;
