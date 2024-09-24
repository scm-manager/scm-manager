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
import { File, Link, Repository } from "@scm-manager/ui-types";
import { Annotate, ErrorNotification, Loading } from "@scm-manager/ui-components";
import { useAnnotations, useContentType } from "@scm-manager/ui-api";
import { determineSyntaxHighlightingLanguage } from "../utils/files";

type Props = {
  file: File;
  repository: Repository;
  revision: string;
};

const AnnotateView: FC<Props> = ({ file, repository, revision }) => {
  const { data: annotation, isLoading: isAnnotationLoading, error: annotationLoadError } = useAnnotations(
    repository,
    revision,
    file
  );
  const { data: contentType, isLoading: isContentTypeLoading, error: contentTypeLoadError } = useContentType(
    (file._links.self as Link).href
  );
  const error = annotationLoadError || contentTypeLoadError;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isAnnotationLoading || isContentTypeLoading || !annotation || !contentType) {
    return <Loading />;
  }

  const language = determineSyntaxHighlightingLanguage(contentType);
  return <Annotate source={{ ...annotation, language }} repository={repository} />;
};

export default AnnotateView;
