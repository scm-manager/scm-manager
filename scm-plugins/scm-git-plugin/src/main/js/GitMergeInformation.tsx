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
