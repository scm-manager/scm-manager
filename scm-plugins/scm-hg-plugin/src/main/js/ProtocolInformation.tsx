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
import { Repository } from "@scm-manager/ui-types";
import {
  ErrorNotification,
  Loading,
  SubSubtitle,
  repositories,
  PreformattedCodeBlock,
} from "@scm-manager/ui-components";
import { useChangesets } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
};

const ProtocolInformation: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  const { data, error, isLoading } = useChangesets(repository, { limit: 1 });
  const href = repositories.getProtocolLinkByType(repository, "http");

  if (!href) {
    return null;
  }

  if (isLoading) {
    return <Loading />;
  }

  const emptyRepository = (data?._embedded?.changesets.length || 0) === 0;

  const hgCloneCommand = `hg clone ${href}`;
  const hgCreateCommand =
    `hg init ${repository.name}\n` +
    `cd ${repository.name}\n` +
    'echo "[paths]" > .hg/hgrc\n' +
    `echo "default = ${href}` +
    '" >> .hg/hgrc\n' +
    `echo "# ${repository.name}` +
    '" > README.md\n' +
    "hg add README.md\n" +
    'hg commit -m "added readme"\n\n' +
    "hg push";
  const hgReplaceCommand =
    "# add the repository url as default to your .hg/hgrc e.g:\n" +
    `default = ${href}\n` +
    "# push to remote repository\n" +
    "hg push";

  return (
    <div className="content">
      <ErrorNotification error={error} />
      <SubSubtitle>{t("scm-hg-plugin.information.clone")}</SubSubtitle>
      <PreformattedCodeBlock>{hgCloneCommand}</PreformattedCodeBlock>
      {emptyRepository && (
        <>
          <SubSubtitle>{t("scm-hg-plugin.information.create")}</SubSubtitle>
          <PreformattedCodeBlock>{hgCreateCommand}</PreformattedCodeBlock>
        </>
      )}
      <SubSubtitle>{t("scm-hg-plugin.information.replace")}</SubSubtitle>
      <PreformattedCodeBlock>{hgReplaceCommand}</PreformattedCodeBlock>
    </div>
  );
};

export default ProtocolInformation;
