import React, { FC, useEffect, useState } from "react";
import { getContent } from "./SourcecodeViewer";
import { Link, File } from "@scm-manager/ui-types";
import { Loading, ErrorNotification, MarkdownView } from "@scm-manager/ui-components";
import styled from "styled-components";

type Props = {
  file: File;
};

const MarkdownContent = styled.div`
  padding: 0.5rem;
`;

const MarkdownViewer: FC<Props> = ({ file }) => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [content, setContent] = useState("");

  useEffect(() => {
    getContent((file._links.self as Link).href)
      .then(content => {
        setLoading(false);
        setContent(content);
      })
      .catch(error => {
        setLoading(false);
        setError(error);
      });
  }, [file]);

  if (loading) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <MarkdownContent>
      <MarkdownView content={content} />
    </MarkdownContent>
  );
};

export default MarkdownViewer;
