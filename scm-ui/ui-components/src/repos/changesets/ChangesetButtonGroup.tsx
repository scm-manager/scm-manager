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
import { Button, ButtonAddons } from "../../buttons";
import { createChangesetLink, createChangesetLinkByBranch, createSourcesLink } from "./changesets";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
  changeset: Changeset;
  file?: File;
  branch?: Branch;
};

const ChangesetButtonGroup = React.forwardRef<HTMLButtonElement | HTMLAnchorElement, Props>(
  ({ repository, changeset, file, branch }, ref) => {
    const [t] = useTranslation("repos");
    const changesetLink = branch
      ? createChangesetLinkByBranch(repository, changeset, branch)
      : createChangesetLink(repository, changeset);
    const sourcesLink = createSourcesLink(repository, changeset, file);
    return (
      <ButtonAddons className="m-0">
        <Button
          className="px-3"
          ref={ref}
          link={changesetLink}
          icon="list-ul"
          label={t("changeset.buttons.details")}
          reducedMobile={true}
        />
        <Button
          className="px-3"
          link={sourcesLink}
          icon="code"
          label={t("changeset.buttons.sources")}
          reducedMobile={true}
        />
      </ButtonAddons>
    );
  }
);

export default ChangesetButtonGroup;
