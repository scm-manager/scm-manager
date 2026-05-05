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
import { ButtonAddons } from "@scm-manager/ui-components";
import { Icon, LinkButton } from "@scm-manager/ui-core";
import { Branch, Repository } from "@scm-manager/ui-types";

type Props = {
  repository: Repository;
  branch: Branch;
};

const BranchButtonGroup: FC<Props> = ({ repository, branch }) => {
  const [t] = useTranslation("repos");

  const changesetLink = `/repo/${repository.namespace}/${repository.name}/code/branch/${encodeURIComponent(
    branch.name
  )}/changesets/`;
  const sourcesLink = `/repo/${repository.namespace}/${repository.name}/code/sources/${encodeURIComponent(
    branch.name
  )}/`;

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
};

export default BranchButtonGroup;
