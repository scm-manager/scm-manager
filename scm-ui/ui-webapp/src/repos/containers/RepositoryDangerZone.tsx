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
import { DangerZone, Subtitle } from "@scm-manager/ui-components";
import RenameRepository from "./RenameRepository";
import DeleteRepo from "./DeleteRepo";
import ArchiveRepo from "./ArchiveRepo";
import UnarchiveRepo from "./UnarchiveRepo";
import { ExtensionPoint, extensionPoints, useBinder } from "@scm-manager/ui-extensions";

type Props = {
  repository: Repository;
};

const RepositoryDangerZone: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("repos");
  const binder = useBinder();

  const dangerZone = [];
  if (repository?._links?.rename || repository?._links?.renameWithNamespace) {
    dangerZone.push(<RenameRepository repository={repository} />);
  }

  if (binder.hasExtension<extensionPoints.RepositoryDeleteButton>("repository.deleteButton", { repository })) {
    dangerZone.push(
      <ExtensionPoint<extensionPoints.RepositoryDeleteButton> name="repository.deleteButton" props={{ repository }} />
    );
  } else if (repository?._links?.delete) {
    dangerZone.push(<DeleteRepo repository={repository} />);
  }
  if (repository?._links?.archive) {
    dangerZone.push(<ArchiveRepo repository={repository} />);
  }
  if (repository?._links?.unarchive) {
    dangerZone.push(<UnarchiveRepo repository={repository} />);
  }
  dangerZone.push(
    <ExtensionPoint<extensionPoints.RepositoryDangerZone>
      name="repository.dangerZone"
      props={{ repository }}
      renderAll={true}
    />
  );

  if (dangerZone.length === 0) {
    return null;
  }

  return (
    <>
      <hr />
      <Subtitle subtitle={t("repositoryForm.dangerZone")} />
      <DangerZone className="px-4 py-5">{dangerZone}</DangerZone>
    </>
  );
};

export default RepositoryDangerZone;
