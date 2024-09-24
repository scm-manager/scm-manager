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
import { Tag, Repository } from "@scm-manager/ui-types";
import { Button, ButtonAddons } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
  tag: Tag;
};

const TagButtonGroup: FC<Props> = ({ repository, tag }) => {
  const [t] = useTranslation("repos");

  const changesetLink = `/repo/${repository.namespace}/${repository.name}/code/changeset/${encodeURIComponent(
    tag.revision
  )}`;
  const sourcesLink = `/repo/${repository.namespace}/${repository.name}/sources/${encodeURIComponent(tag.revision)}/`;

  return (
    <>
      <ButtonAddons>
        <Button link={changesetLink} icon="exchange-alt" label={t("tag.commit")} reducedMobile={true} />
        <Button link={sourcesLink} icon="code" label={t("tag.sources")} reducedMobile={true} />
      </ButtonAddons>
    </>
  );
};

export default TagButtonGroup;
