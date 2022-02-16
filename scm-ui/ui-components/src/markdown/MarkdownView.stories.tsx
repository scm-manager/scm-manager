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
import { storiesOf } from "@storybook/react";
import MarkdownView from "./MarkdownView";
import styled from "styled-components";

import TestPage from "../__resources__/test-page.md";
import MarkdownWithoutLang from "../__resources__/markdown-without-lang.md";
import MarkdownXmlCodeBlock from "../__resources__/markdown-xml-codeblock.md";
import MarkdownUmlCodeBlock from "../__resources__/markdown-uml-codeblock.md";
import MarkdownInlineXml from "../__resources__/markdown-inline-xml.md";
import MarkdownLinks from "../__resources__/markdown-links.md";
import MarkdownCommitLinks from "../__resources__/markdown-commit-link.md";
import MarkdownXss from "../__resources__/markdown-xss.md";
import MarkdownChangelog from "../__resources__/markdown-changelog.md";
import Title from "../layout/Title";
import { Subtitle } from "../layout";
import { MemoryRouter } from "react-router-dom";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
import { ProtocolLinkRendererExtension, ProtocolLinkRendererProps } from "./markdownExtensions";

const Spacing = styled.div`
  padding: 2em;
`;

storiesOf("MarkdownView", module)
  .addDecorator(story => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator(story => <Spacing>{story()}</Spacing>)
  // Add async parameter, because the tests needs to render async before snapshot is taken so that
  // code fragments get highlighted properly
  .addParameters({ storyshots: { async: true } })
  .add("Default", () => <MarkdownView content={TestPage} skipHtml={false} />)
  .add("Skip Html", () => <MarkdownView content={TestPage} skipHtml={true} />)
  .add("Code without Lang", () => <MarkdownView content={MarkdownWithoutLang} skipHtml={false} />)
  .add("Xml Code Block", () => <MarkdownView content={MarkdownXmlCodeBlock} />)
  .add("Inline Xml", () => (
    <>
      <Title title="Inline Xml" />
      <Subtitle subtitle="Inline xml outside of a code block is not supported" />
      <MarkdownView content={MarkdownInlineXml} />
    </>
  ))
  .add("Links", () => {
    const binder = new Binder("custom protocol link renderer");
    binder.bind("markdown-renderer.link.protocol", {
      protocol: "scw",
      renderer: ProtocolLinkRenderer
    } as ProtocolLinkRendererExtension);
    return (
      <BinderContext.Provider value={binder}>
        <MarkdownView content={MarkdownLinks} basePath="/scm/" />
      </BinderContext.Provider>
    );
  })
  .add("Links without Base Path", () => {
    const binder = new Binder("custom protocol link renderer");
    binder.bind("markdown-renderer.link.protocol", {
      protocol: "scw",
      renderer: ProtocolLinkRenderer
    } as ProtocolLinkRendererExtension);
    return (
      <BinderContext.Provider value={binder}>
        <MarkdownView content={MarkdownLinks} />
      </BinderContext.Provider>
    );
  })
  .add("Header Anchor Links", () => (
    <MarkdownView
      content={MarkdownChangelog}
      basePath={"/"}
      permalink={"/?path=/story/markdownview--header-anchor-links"}
      enableAnchorHeadings={true}
    />
  ))
  .add("Commit Links", () => <MarkdownView content={MarkdownCommitLinks} />)
  .add("Custom code renderer", () => {
    const binder = new Binder("custom code renderer");
    const Container: FC<{ value: string }> = ({ value }) => {
      return (
        <div>
          <h4 style={{ border: "1px dashed lightgray", padding: "2px" }}>
            To render plantuml as images within markdown, please install the scm-markdown-plantuml-plguin
          </h4>
          <pre>{value}</pre>
        </div>
      );
    };
    binder.bind("markdown-renderer.code.uml", Container);
    return (
      <BinderContext.Provider value={binder}>
        <MarkdownView content={MarkdownUmlCodeBlock} />
      </BinderContext.Provider>
    );
  })
  .add("XSS Prevention", () => <MarkdownView content={MarkdownXss} skipHtml={false} />);

export const ProtocolLinkRenderer: FC<ProtocolLinkRendererProps> = ({ protocol, href, children }) => {
  return (
    <div style={{ border: "1px dashed lightgray", padding: "2px" }}>
      <h4>
        Link: {href} [Protocol: {protocol}]
      </h4>
      <div>children: {children}</div>
    </div>
  );
};
