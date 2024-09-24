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
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import TagDetail from "./TagDetail";
import TagDangerZone from "../container/TagDangerZone";

type Props = {
  repository: Repository;
  tag: Tag;
};

const TagView: FC<Props> = ({ repository, tag }) => {
  return (
    <>
      <TagDetail tag={tag} repository={repository} />
      <hr />
      <div className="content">
        <ExtensionPoint<extensionPoints.RepositoryTagDetailsInformation>
          name="repos.tag-details.information"
          renderAll={true}
          props={{
            repository,
            tag
          }}
        />
      </div>
      <TagDangerZone repository={repository} tag={tag} />
    </>
  );
};

export default TagView;
