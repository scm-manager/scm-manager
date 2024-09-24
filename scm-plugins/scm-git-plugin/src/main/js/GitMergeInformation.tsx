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

import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository } from "@scm-manager/ui-types";
import { PreformattedCodeBlock, SubSubtitle } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
  target: string;
  source: string;
};

class GitMergeInformation extends React.Component<Props> {
  render() {
    const { source, target, t } = this.props;

    const gitCheckoutCommand = `git checkout ${target}`;
    const gitUpdateCommand = "git pull";
    const gitMergeCommand = `git merge origin/${source}`;
    const gitResolveCommandA = "git add <conflict file>";
    const gitResolveCommandB = "git add --all";
    const gitCommitCommand = `git commit -m "Merge ${source} into ${target}"`;
    const gitPushCommand = "git push";

    return (
      <div>
        <SubSubtitle>{t("scm-git-plugin.information.merge.heading")}</SubSubtitle>
        {t("scm-git-plugin.information.merge.checkout")}
        <PreformattedCodeBlock>{gitCheckoutCommand}</PreformattedCodeBlock>
        {t("scm-git-plugin.information.merge.update")}
        <PreformattedCodeBlock>{gitUpdateCommand}</PreformattedCodeBlock>
        {t("scm-git-plugin.information.merge.merge")}
        <PreformattedCodeBlock>{gitMergeCommand}</PreformattedCodeBlock>
        {t("scm-git-plugin.information.merge.resolve_a")}
        <PreformattedCodeBlock>{gitResolveCommandA}</PreformattedCodeBlock>
        {t("scm-git-plugin.information.merge.resolve_b")}
        <PreformattedCodeBlock>{gitResolveCommandB}</PreformattedCodeBlock>
        {t("scm-git-plugin.information.merge.commit")}
        <PreformattedCodeBlock>{gitCommitCommand}</PreformattedCodeBlock>
        {t("scm-git-plugin.information.merge.push")}
        <PreformattedCodeBlock>{gitPushCommand}</PreformattedCodeBlock>
      </div>
    );
  }
}

export default withTranslation("plugins")(GitMergeInformation);
