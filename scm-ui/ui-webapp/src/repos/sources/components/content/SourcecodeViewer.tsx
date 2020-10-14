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
import { apiClient, ErrorNotification, Loading, SyntaxHighlighter } from "@scm-manager/ui-components";
import { File, Link } from "@scm-manager/ui-types";

type Props = {
  file: File;
  language: string;
};

const SourcecodeViewer: FC<Props> = ({ file, language }) => {
  const [content, setContent] = useState("");
  const [error, setError] = useState<Error | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const [currentFileRevision, setCurrentFileRevision] = useState("");

  useEffect(() => {
    function fetchContent() {
      getContent((file._links.self as Link).href)
        .then(content => {
          setLoading(false);
          setContent(content);
          setCurrentFileRevision(file.revision);
        })
        .catch(error => {
          setLoading(false);
          setError(error);
        });
    }

    if (file.revision !== currentFileRevision) {
      fetchContent();
    }
  });

  if (loading) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!content) {
    return null;
  }

  return <SyntaxHighlighter language={getLanguage(language)} value={content} />;
};

export function getLanguage(language: string) {
  return language.toLowerCase();
}

export function getContent(url: string) {
  return apiClient.get(url).then(response => response.text());
}

export default SourcecodeViewer;
