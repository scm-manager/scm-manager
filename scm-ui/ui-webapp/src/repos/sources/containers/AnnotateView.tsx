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
  const {
    data: annotation,
    isLoading: isAnnotationLoading,
    error: annotationLoadError,
  } = useAnnotations(repository, revision, file);
  const {
    data: contentType,
    isLoading: isContentTypeLoading,
    error: contentTypeLoadError,
  } = useContentType((file._links.self as Link).href);
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
