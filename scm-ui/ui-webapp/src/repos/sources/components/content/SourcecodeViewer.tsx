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
import { useLocation } from "react-router-dom";

type Props = {
  file: File;
  language: string;
};

const SourcecodeViewer: FC<Props> = ({ file, language }) => {
  const [content, setContent] = useState("");
  const [error, setError] = useState<Error | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const [currentFileRevision, setCurrentFileRevision] = useState("");
  const location = useLocation();

  useEffect(() => {
    if (file.revision !== currentFileRevision) {
      getContent((file._links.self as Link).href)
        .then(content => {
          setContent(content);
          setCurrentFileRevision(file.revision);
          setLoading(false);
        })
        .catch(setError);
    }
  }, [currentFileRevision, file]);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  if (!content) {
    return null;
  }

  const pathParts = location.pathname.split("/");
  pathParts[6] = currentFileRevision;
  const permaLink: string = pathParts.join("/");
  const createPermalink = () => permaLink;

  return <SyntaxHighlighter language={getLanguage(language)} value={content} createPermaLink={createPermalink} />;
};

export function getLanguage(language: string) {
  return language.toLowerCase();
}

export function getContent(url: string) {
  return apiClient.get(url).then(response => response.text());
}

export default SourcecodeViewer;
