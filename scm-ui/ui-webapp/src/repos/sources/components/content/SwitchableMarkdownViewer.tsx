import React, { FC, useState } from "react";
import styled from "styled-components";
import MarkdownViewer from "./MarkdownViewer";
import SourcecodeViewer from "./SourcecodeViewer";
import { File } from "@scm-manager/ui-types";
import { Button } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

const ToggleButton = styled(Button)`
  max-width: 1rem;
  position: absolute;
  top: 0;
  right: 0.25rem;
  z-index: 30;
`;

const Container = styled.div`
  position: relative;
`;

type Props = {
  file: File;
};

const SwitchableMarkdownViewer: FC<Props> = ({ file }) => {
  const { t } = useTranslation("repos");
  const [renderMarkdown, setRenderMarkdown] = useState(true);

  const toggleMarkdown = () => {
    setRenderMarkdown(!renderMarkdown);
  };

  return (
    <Container>
      <ToggleButton
        color={renderMarkdown ? "link" : ""}
        action={toggleMarkdown}
        title={
          renderMarkdown
            ? t("sources.content.toggleButton.showSources")
            : t("sources.content.toggleButton.showMarkdown")
        }
      >
        <i className="fab fa-markdown" />
      </ToggleButton>
      {renderMarkdown ? <MarkdownViewer file={file} /> : <SourcecodeViewer file={file} language={"MARKDOWN"} />}
    </Container>
  );
};

export default SwitchableMarkdownViewer;
