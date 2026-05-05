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

// import React from "react";
// import { storiesOf } from "@storybook/react";
// import { bashHit, filenameXmlHit, javaHit, markdownHit, pullRequestHit } from "../__resources__/SearchHit";
// import TextHitField from "./TextHitField";
//
// storiesOf("TextHitField", module)
//   .add("Default", () => (
//     <pre>
//       <TextHitField hit={javaHit} field="content" />
//     </pre>
//   ))
//   .add("Java SyntaxHighlighting", () => (
//     <pre>
//       <TextHitField hit={javaHit} field="content" syntaxHighlightingLanguage="java" />
//     </pre>
//   ))
//   .add("Bash SyntaxHighlighting", () => (
//     <pre>
//       <TextHitField hit={bashHit} field="content" syntaxHighlightingLanguage="bash" />
//     </pre>
//   ))
//   .add("Markdown SyntaxHighlighting", () => (
//     <pre>
//       <TextHitField hit={markdownHit} field="content" syntaxHighlightingLanguage="markdown" />
//     </pre>
//   ))
//   .add("Unknown SyntaxHighlighting", () => (
//     <pre>
//       <TextHitField hit={bashHit} field="content" syntaxHighlightingLanguage="__unknown__" />
//     </pre>
//   ))
//   .add("Non Content Search", () => (
//     <pre>
//       <TextHitField hit={filenameXmlHit} field="content" syntaxHighlightingLanguage="xml" />
//     </pre>
//   ))
//   .add("Truncate", () => (
//     <pre>
//       <TextHitField hit={pullRequestHit} field="description" truncateValueAt={128} />
//     </pre>
//   ))
//   .add("Truncate Keep Whole Line", () => (
//     <pre>
//       <TextHitField hit={filenameXmlHit} field="content" syntaxHighlightingLanguage="xml" truncateValueAt={1024} />
//     </pre>
//   ));

import React from "react";
import type { Meta, StoryObj } from "@storybook/react";

import TextHitField from "./TextHitField";
import { bashHit, filenameXmlHit, javaHit, markdownHit, pullRequestHit } from "../__resources__/SearchHit";

// --- Storybook Metadata ---

const meta: Meta<typeof TextHitField> = {
  title: "Global/TextHitField",
  component: TextHitField,
  // The <pre> tag is now a decorator, applied to all stories in this file
  decorators: [(Story) => <pre>{Story()}</pre>],
  tags: ["autodocs"],
};

export default meta;

// --- Story Definitions ---

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    hit: javaHit,
    field: "content",
  },
};

export const JavaSyntaxHighlighting: Story = {
  name: "Java Syntax Highlighting",
  args: {
    ...Default.args,
    syntaxHighlightingLanguage: "java",
  },
};

export const BashSyntaxHighlighting: Story = {
  name: "Bash Syntax Highlighting",
  args: {
    hit: bashHit,
    field: "content",
    syntaxHighlightingLanguage: "bash",
  },
};

export const MarkdownSyntaxHighlighting: Story = {
  name: "Markdown Syntax Highlighting",
  args: {
    hit: markdownHit,
    field: "content",
    syntaxHighlightingLanguage: "markdown",
  },
};

export const UnknownSyntaxHighlighting: Story = {
  name: "Unknown Syntax Highlighting",
  args: {
    ...BashSyntaxHighlighting.args,
    syntaxHighlightingLanguage: "__unknown__",
  },
};

export const NonContentSearch: Story = {
  name: "Non-Content Search",
  args: {
    hit: filenameXmlHit,
    field: "content",
    syntaxHighlightingLanguage: "xml",
  },
};

export const Truncate: Story = {
  args: {
    hit: pullRequestHit,
    field: "description",
    truncateValueAt: 128,
  },
};

export const TruncateKeepWholeLine: Story = {
  name: "Truncate (Keep Whole Line)",
  args: {
    ...NonContentSearch.args,
    truncateValueAt: 1024,
  },
};
