import React, { FC, useEffect, useState } from "react";
import { getContent } from "./SourcecodeViewer";
import { Link, File } from "@scm-manager/ui-types";
import { Loading, ErrorNotification, MarkdownView, Button, Level } from "@scm-manager/ui-components";

type Props = {
  file: File;
};

const MarkdownViewer: FC<Props> = ({ file }) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | undefined>(undefined);
  const [content, setContent] = useState<string>("");

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

  return <MarkdownView content={content} />;
};

export default MarkdownViewer;
