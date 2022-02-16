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
import React from "react";
import { storiesOf } from "@storybook/react";
import { bashHit, filenameXmlHit, javaHit, markdownHit, pullRequestHit } from "../__resources__/SearchHit";
import TextHitField from "./TextHitField";

storiesOf("TextHitField", module)
  .add("Default", () => (
    <pre>
      <TextHitField hit={javaHit} field={"content"} />
    </pre>
  ))
  .add("Java SyntaxHighlighting", () => (
    <pre>
      <TextHitField hit={javaHit} field={"content"} syntaxHighlightingLanguage="java" />
    </pre>
  ))
  .add("Bash SyntaxHighlighting", () => (
    <pre>
      <TextHitField hit={bashHit} field={"content"} syntaxHighlightingLanguage="bash" />
    </pre>
  ))
  .add("Markdown SyntaxHighlighting", () => (
    <pre>
      <TextHitField hit={markdownHit} field={"content"} syntaxHighlightingLanguage="markdown" />
    </pre>
  ))
  .add("Unknown SyntaxHighlighting", () => (
    <pre>
      <TextHitField hit={bashHit} field={"content"} syntaxHighlightingLanguage="__unknown__" />
    </pre>
  ))
  .add("Non Content Search", () => (
    <pre>
      <TextHitField hit={filenameXmlHit} field={"content"} syntaxHighlightingLanguage="xml" />
    </pre>
  ))
  .add("Truncate", () => (
    <pre>
      <TextHitField hit={pullRequestHit} field={"description"} truncateValueAt={128} />
    </pre>
  ))
  .add("Truncate Keep Whole Line", () => (
    <pre>
      <TextHitField hit={filenameXmlHit} field={"content"} syntaxHighlightingLanguage="xml" truncateValueAt={1024} />
    </pre>
  ));
