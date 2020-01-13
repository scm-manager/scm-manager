import React from "react";
import { storiesOf } from "@storybook/react";
import MarkdownView from "./MarkdownView";
import styled from "styled-components";

import TestPage from "./__resources__/test-page.md";
import MarkdownWithoutLang from "./__resources__/markdown-without-lang.md";

const Spacing = styled.div`
  padding: 2em;
`;

storiesOf("MarkdownView", module)
  .add("Default", () => (
    <Spacing>
      <MarkdownView content={TestPage} skipHtml={false} />
    </Spacing>
  ))
  .add("Code without Lang", () => (
    <Spacing>
      <MarkdownView content={MarkdownWithoutLang} skipHtml={false} />
    </Spacing>
  ));
