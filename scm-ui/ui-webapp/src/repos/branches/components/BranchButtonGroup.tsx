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
import { Branch, Repository } from "@scm-manager/ui-types";
import { ButtonAddons } from "@scm-manager/ui-components";
import { encodePart } from "../../sources/components/content/FileLink";
import { Icon, LinkButton } from "@scm-manager/ui-core";

type Props = WithTranslation & {
  repository: Repository;
  branch: Branch;
};

class BranchButtonGroup extends React.Component<Props> {
  render() {
    const { repository, branch, t } = this.props;

    const changesetLink = `/repo/${repository.namespace}/${repository.name}/branch/${encodePart(
      branch.name
    )}/changesets/`;
    const sourcesLink = `/repo/${repository.namespace}/${repository.name}/sources/${encodePart(branch.name)}/`;

    return (
      <ButtonAddons>
        <LinkButton to={changesetLink}>
          <Icon>exchange-alt</Icon>
          <span>{t("branch.commits")}</span>
        </LinkButton>
        <LinkButton to={sourcesLink}>
          <Icon>code</Icon>
          <span>{t("branch.sources")}</span>
        </LinkButton>
      </ButtonAddons>
    );
  }
}

export default withTranslation("repos")(BranchButtonGroup);
