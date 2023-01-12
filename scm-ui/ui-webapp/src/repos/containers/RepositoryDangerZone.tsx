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
