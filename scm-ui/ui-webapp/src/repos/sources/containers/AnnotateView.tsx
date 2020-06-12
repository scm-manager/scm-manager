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

import React, { FC, useEffect, useState } from "react";
import { Link, Repository, File } from "@scm-manager/ui-types";
import { Annotate, apiClient, ErrorNotification, Loading } from "@scm-manager/ui-components";
import { getContentType } from "./contentType";
import { AnnotatedSource } from "@scm-manager/ui-components/src/Annotate";

type Props = {
  file: File;
  repository: Repository;
};

const AnnotateView: FC<Props> = ({ file, repository }) => {
  const [annotation, setAnnotation] = useState<AnnotatedSource>(undefined);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [language, setLanguage] = useState<string | undefined>(undefined);

  useEffect(() => {
    getContentType(file._links.self.href)
      .then(result => {
        setLanguage(result.language);
      })
      .catch(error => {
        setError(error);
      });

    apiClient
      .get((file._links.annotate as Link).href)
      .then(response => response.json())
      .then(content => {
        setAnnotation(content);
      })
      .catch(error => {
        setError(error);
      });
  }, [file]);

  if (error) {
    return <ErrorNotification error={error} />;
  } else if (annotation && language) {
    return <Annotate source={{ ...annotation, language }} repository={repository} />;
  } else {
    return <Loading />;
  }
};

export default AnnotateView;
