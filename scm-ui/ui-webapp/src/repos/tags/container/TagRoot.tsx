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
import { useParams } from "react-router-dom";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import TagView from "../components/TagView";
import { useTag } from "@scm-manager/ui-api";
import { useRepositoryFromParams } from "@scm-manager/ui-core";

type Params = {
  tag: string;
};

const TagRoot: FC = () => {
  const repository = useRepositoryFromParams();
  const { tag: tagName } = useParams<Params>();
  const { isLoading, error, data: tag } = useTag(repository, tagName!);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || !tag) {
    return <Loading />;
  }

  return <TagView repository={repository} tag={tag} />;
};
export default TagRoot;
