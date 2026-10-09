/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

// import React, { FC } from "react";
// import { storiesOf } from "@storybook/react";
// import MarkdownView from "./MarkdownView";
// import styled from "styled-components";
//
// import TestPage from "../__resources__/test-page.md";
// import MarkdownWithoutLang from "../__resources__/markdown-without-lang.md";
// import MarkdownXmlCodeBlock from "../__resources__/markdown-xml-codeblock.md";
// import MarkdownUmlCodeBlock from "../__resources__/markdown-uml-codeblock.md";
// import MarkdownInlineXml from "../__resources__/markdown-inline-xml.md";
// import MarkdownLinks from "../__resources__/markdown-links.md";
// import MarkdownImages from "../__resources__/markdown-images.md";
// import MarkdownCommitLinks from "../__resources__/markdown-commit-link.md";
// import MarkdownXss from "../__resources__/markdown-xss.md";
// import MarkdownChangelog from "../__resources__/markdown-changelog.md";
// import Title from "../layout/Title";
// import { Subtitle } from "../layout";
// import { MemoryRouter } from "react-router-dom";
// import { Binder, BinderContext, extensionPoints } from "@scm-manager/ui-extensions";
// import { ProtocolLinkRendererProps } from "./markdownExtensions";
// import { RepositoryContextProvider, RepositoryRevisionContextProvider } from "@scm-manager/ui-api";
//
// const Spacing = styled.div`
//   padding: 2em;
// `;
//
// storiesOf("MarkdownView", module)
//   .addDecorator(story => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
//   .addDecorator(story => <Spacing>{story()}</Spacing>)
//   // Add async parameter, because the tests needs to render async before snapshot is taken so that
//   // code fragments get highlighted properly
//   .addParameters({ storyshots: { async: true } })
//   .add("Default", () => <MarkdownView content={TestPage} skipHtml={false} />)
//   .add("Skip Html", () => <MarkdownView content={TestPage} skipHtml={true} />)
//   .add("Code without Lang", () => <MarkdownView content={MarkdownWithoutLang} skipHtml={false} />)
//   .add("Xml Code Block", () => <MarkdownView content={MarkdownXmlCodeBlock} />)
//   .add("Inline Xml", () => (
//     <>
//       <Title title="Inline Xml" />
//       <Subtitle subtitle="Inline xml outside of a code block is not supported" />
//       <MarkdownView content={MarkdownInlineXml} />
//     </>
//   ))
//   .add("Links", () => {
//     const binder = new Binder("custom protocol link renderer");
//     binder.bind<extensionPoints.MarkdownLinkProtocolRenderer<"scw">>("markdown-renderer.link.protocol", {
//       protocol: "scw",
//       renderer: ProtocolLinkRenderer
//     });
//     return (
//       <BinderContext.Provider value={binder}>
//         <MarkdownView content={MarkdownLinks} basePath="/scm/" />
//       </BinderContext.Provider>
//     );
//   })
//   .add("Links without Base Path", () => {
//     const binder = new Binder("custom protocol link renderer");
//     binder.bind<extensionPoints.MarkdownLinkProtocolRenderer<"scw">>("markdown-renderer.link.protocol", {
//       protocol: "scw",
//       renderer: ProtocolLinkRenderer
//     });
//     return (
//       <BinderContext.Provider value={binder}>
//         <MarkdownView content={MarkdownLinks} />
//       </BinderContext.Provider>
//     );
//   })
//   .add("Header Anchor Links", () => (
//     <MarkdownView
//       content={MarkdownChangelog}
//       basePath="/"
//       permalink="/?path=/story/markdownview--header-anchor-links"
//       enableAnchorHeadings={true}
//     />
//   ))
//   .add("Commit Links", () => <MarkdownView content={MarkdownCommitLinks} />)
//   .add("Custom code renderer", () => {
//     const binder = new Binder("custom code renderer");
//     const Container: FC<{ value: string }> = ({ value }) => {
//       return (
//         <div>
//           <h4 style={{ border: "1px dashed lightgray", padding: "2px" }}>
//             To render plantuml as images within markdown, please install the scm-markdown-plantuml-plugin
//           </h4>
//           <pre>{value}</pre>
//         </div>
//       );
//     };
//     binder.bind<extensionPoints.MarkdownCodeRenderer<"uml">>("markdown-renderer.code.uml", Container);
//     return (
//       <BinderContext.Provider value={binder}>
//         <MarkdownView content={MarkdownUmlCodeBlock} />
//       </BinderContext.Provider>
//     );
//   })
//   .add("XSS Prevention", () => <MarkdownView content={MarkdownXss} skipHtml={false} />)
//   .add("Images", () => (
//     <RepositoryContextProvider
//       // @ts-ignore We do not need a valid repository here, only one with a content link
//       repository={{
//         _links: { content: { href: "https://my.scm/scm/api/v2/some/repository/content/{revision}/{path}" } }
//       }}
//     >
//       <RepositoryRevisionContextProvider revision="42">
//         <MarkdownView basePath="/scm/" content={MarkdownImages} />
//       </RepositoryRevisionContextProvider>
//     </RepositoryContextProvider>
//   ));
//
// export const ProtocolLinkRenderer: FC<ProtocolLinkRendererProps<"scw">> = ({ protocol, href, children }) => {
//   return (
//     <div style={{ border: "1px dashed lightgray", padding: "2px" }}>
//       <h4>
//         Link: {href} [Protocol: {protocol}]
//       </h4>
//       <div>children: {children}</div>
//     </div>
//   );
// };

import React, { FC } from "react";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";
import { Binder, BinderContext, extensionPoints } from "@scm-manager/ui-extensions";
import { RepositoryContextProvider, RepositoryRevisionContextProvider } from "@scm-manager/ui-api";

import MarkdownView from "./MarkdownView";
import Title from "../layout/Title";
import { Subtitle } from "../layout";
import { ProtocolLinkRendererProps } from "./markdownExtensions";

import TestPage from "../__resources__/test-page.md";
import MarkdownWithoutLang from "../__resources__/markdown-without-lang.md";
import MarkdownXmlCodeBlock from "../__resources__/markdown-xml-codeblock.md";
import MarkdownUmlCodeBlock from "../__resources__/markdown-uml-codeblock.md";
import MarkdownInlineXml from "../__resources__/markdown-inline-xml.md";
import MarkdownLinks from "../__resources__/markdown-links.md";
import MarkdownImages from "../__resources__/markdown-images.md";
import MarkdownCommitLinks from "../__resources__/markdown-commit-link.md";
import MarkdownXss from "../__resources__/markdown-xss.md";
import MarkdownChangelog from "../__resources__/markdown-changelog.md";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { http, HttpResponse } from "msw";

// --- Helfer-Komponenten und Mock-Daten (aus der Original-Story übernommen) ---

const Spacing = styled.div`
  padding: 2em;
`;

const ProtocolLinkRenderer: FC<ProtocolLinkRendererProps<"scw">> = ({ protocol, href, children }) => (
  <div style={{ border: "1px dashed lightgray", padding: "2px" }}>
    <h4>
      Link: {href} [Protocol: {protocol}]
    </h4>
    <div>children: {children}</div>
  </div>
);

// --- Storybook Metadaten ---

const meta: Meta<typeof MarkdownView> = {
  title: "Global/MarkdownView",
  component: MarkdownView,
  decorators: [
    (Story) => (
      <MemoryRouter initialEntries={["/repo/storybook/markdown"]}>
        <Routes>
          <Route path="/repo/:namespace/:name" element={<Story />} />
        </Routes>
      </MemoryRouter>
    ),
    (Story) => (
      <Spacing>
        <Story />
      </Spacing>
    ),
  ],
  tags: ["autodocs"],
  parameters: {
    msw: {
      handlers: {
        index: http.get("/api/v2/", () =>
          HttpResponse.json({
            version: "x.y.z",
            instanceId: "storybook",
            _links: { repositories: { href: "/repositories" } },
          }),
        ),
        repository: http.get("/api/v2/repositories/storybook/markdown", () =>
          HttpResponse.json({
            namespace: "storybook",
            name: "markdown",
            _links: { content: { href: "/api/v2/repositories/storybook/markdown/content/{revision}/{path}" } },
          }),
        ),
      },
    },
  },
};

export default meta;

// --- Story-Definitionen ---

type Story = StoryObj<typeof meta>;

// Einfache Stories, die `args` verwenden
export const Default: Story = {
  args: { content: TestPage, skipHtml: false },
};

export const SkipHtml: Story = {
  args: { content: TestPage, skipHtml: true },
};

export const CodeWithoutLanguage: Story = {
  name: "Code Without Language",
  args: { content: MarkdownWithoutLang, skipHtml: false },
};

export const XmlCodeBlock: Story = {
  args: { content: MarkdownXmlCodeBlock },
};

export const CommitLinks: Story = {
  args: { content: MarkdownCommitLinks },
};

export const HeaderAnchorLinks: Story = {
  args: {
    content: MarkdownChangelog,
    basePath: "/",
    permalink: "/?path=/story/markdownview--header-anchor-links",
    enableAnchorHeadings: true,
  },
};

export const XSSPrevention: Story = {
  args: { content: MarkdownXss, skipHtml: false },
};

// Komplexe Stories, die eine `render`-Funktion benötigen
export const InlineXml: Story = {
  render: () => (
    <>
      <Title title="Inline Xml" />
      <Subtitle subtitle="Inline xml outside of a code block is not supported" />
      <MarkdownView content={MarkdownInlineXml} />
    </>
  ),
};

export const Links: Story = {
  render: () => {
    const binder = new Binder("custom protocol link renderer");
    binder.bind<extensionPoints.MarkdownLinkProtocolRenderer<"scw">>("markdown-renderer.link.protocol", {
      protocol: "scw",
      renderer: ProtocolLinkRenderer,
    });
    return (
      <BinderContext.Provider value={binder}>
        <MarkdownView content={MarkdownLinks} basePath="/scm/" />
      </BinderContext.Provider>
    );
  },
};

export const CustomCodeRenderer: Story = {
  name: "Custom Code Renderer (UML)",
  render: () => {
    const binder = new Binder("custom code renderer");
    const Container: FC<{ value: string }> = ({ value }) => (
      <div>
        <h4 style={{ border: "1px dashed lightgray", padding: "2px" }}>
          To render plantuml as images within markdown, please install the scm-markdown-plantuml-plugin
        </h4>
        <pre>{value}</pre>
      </div>
    );
    binder.bind<extensionPoints.MarkdownCodeRenderer<"uml">>("markdown-renderer.code.uml", Container);
    return (
      <BinderContext.Provider value={binder}>
        <MarkdownView content={MarkdownUmlCodeBlock} />
      </BinderContext.Provider>
    );
  },
};

export const Images: Story = {
  render: () => (
    <RepositoryContextProvider
      // @ts-ignore We do not need a valid repository here
      repository={{
        _links: { content: { href: "https://my.scm/scm/api/v2/some/repository/content/{revision}/{path}" } },
      }}
    >
      <RepositoryRevisionContextProvider revision="42">
        <MarkdownView basePath="/scm/" content={MarkdownImages} />
      </RepositoryRevisionContextProvider>
    </RepositoryContextProvider>
  ),
};
