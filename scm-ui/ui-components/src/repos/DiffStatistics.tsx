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
    </DiffStatisticsContainer>
  );
};

export default DiffStatistics;
