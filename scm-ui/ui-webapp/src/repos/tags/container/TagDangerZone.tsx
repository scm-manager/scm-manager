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
import { Repository, Tag } from "@scm-manager/ui-types";
import { DangerZone, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import DeleteTag from "./DeleteTag";

type Props = {
  repository: Repository;
  tag: Tag;
};

const TagDangerZone: FC<Props> = ({ repository, tag }) => {
  const [t] = useTranslation("repos");

  const dangerZone = [];

  if (tag?._links?.delete) {
    dangerZone.push(<DeleteTag repository={repository} tag={tag} key={dangerZone.length} />);
  }

  if (dangerZone.length === 0) {
    return null;
  }

  return (
    <>
      <hr />
      <Subtitle subtitle={t("tag.dangerZone")} />
      <DangerZone className="px-4 py-5">{dangerZone}</DangerZone>
    </>
  );
};

export default TagDangerZone;
