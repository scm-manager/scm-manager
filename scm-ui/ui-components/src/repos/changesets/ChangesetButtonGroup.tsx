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
import { Branch, Changeset, File, Repository } from "@scm-manager/ui-types";
import { createChangesetLink, createChangesetLinkByBranch, createSourcesLink } from "./changesets";
import { useTranslation } from "react-i18next";
import { Icon, LinkButton } from "@scm-manager/ui-core";
import { ButtonAddons } from "../../buttons";

type Props = {
  repository: Repository;
  changeset: Changeset;
  file?: File;
  branch?: Branch;
};

const ChangesetButtonGroup = React.forwardRef<HTMLAnchorElement, Props>(
  ({ repository, changeset, file, branch }, ref) => {
    const [t] = useTranslation("repos");
    const changesetLink = branch
      ? createChangesetLinkByBranch(repository, changeset, branch)
      : createChangesetLink(repository, changeset);
    const sourcesLink = createSourcesLink(repository, changeset, file);
    return (
      <ButtonAddons>
        <LinkButton className="px-3 pl-5" ref={ref} to={changesetLink}>
          <Icon>list-ul</Icon>
          <span>{t("changeset.buttons.details")}</span>
        </LinkButton>
        <LinkButton className="px-3 pl-5" to={sourcesLink}>
          <Icon>code</Icon>
          <span>{t("changeset.buttons.sources")}</span>
        </LinkButton>
      </ButtonAddons>
    );
  }
);

export default ChangesetButtonGroup;
