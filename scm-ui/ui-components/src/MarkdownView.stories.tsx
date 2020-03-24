import React from "react";
import { storiesOf } from "@storybook/react";
import MarkdownView from "./MarkdownView";
import styled from "styled-components";

import TestPage from "./__resources__/test-page.md";
import MarkdownWithoutLang from "./__resources__/markdown-without-lang.md";
import MarkdownXmlCodeBlock from "./__resources__/markdown-xml-codeblock.md";
import MarkdownInlineXml from "./__resources__/markdown-inline-xml.md";
import Title from "./layout/Title";
import { Subtitle } from "./layout";

const Spacing = styled.div`
  padding: 2em;
`;

storiesOf("MarkdownView", module)
  .addDecorator(story => <Spacing>{story()}</Spacing>)
  .add("Default", () => <MarkdownView content={TestPage} skipHtml={false} />)
  .add("Code without Lang", () => <MarkdownView content={MarkdownWithoutLang} skipHtml={false} />)
  .add("Xml Code Block", () => <MarkdownView content={MarkdownXmlCodeBlock} />)
  .add("Inline Xml", () => (
    <>
      <Title title="Inline Xml" />
      <Subtitle subtitle="Inline xml outside of a code block is not supported" />
      <MarkdownView content={MarkdownInlineXml} />
    </>
  ));
