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
import { Branch } from "@scm-manager/ui-types";
import { PreformattedCodeBlock, SubSubtitle } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  branch: Branch;
};

class GitBranchInformation extends React.Component<Props> {
  render() {
    const { branch, t } = this.props;

    const gitFetchCommand = "git fetch";
    const gitCheckoutCommand = "git checkout " + branch.name;

    return (
      <div>
        <SubSubtitle>{t("scm-git-plugin.information.fetch")}</SubSubtitle>
        <PreformattedCodeBlock>{gitFetchCommand}</PreformattedCodeBlock>
        <SubSubtitle>{t("scm-git-plugin.information.checkout")}</SubSubtitle>
        <PreformattedCodeBlock>{gitCheckoutCommand}</PreformattedCodeBlock>
      </div>
    );
  }
}

export default withTranslation("plugins")(GitBranchInformation);
