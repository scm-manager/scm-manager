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
import React, { ReactNode } from "react";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import SyntaxHighlighter from "./SyntaxHighlighter";

import JavaHttpServer from "./__resources__/HttpServer.java";
import GoHttpServer from "./__resources__/HttpServer.go";
import JsHttpServer from "./__resources__/HttpServer.js";
import PyHttpServer from "./__resources__/HttpServer.py";
import Markdown from "./__resources__/test-page.md";
import { MemoryRouter } from "react-router-dom";

const Spacing = styled.div`
  padding: 1em;
`;

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("SyntaxHighlighter", module)
  .addDecorator(RoutingDecorator)
  // Add async parameter, because the tests needs to render async before snapshot is taken so that
  // code fragments get highlighted properly
  .addParameters({ storyshots: { async: true } })
  .add("Java", () => (
    <Spacing>
      <SyntaxHighlighter language="java" value={JavaHttpServer} />
    </Spacing>
  ))
  .add("Go", () => (
    <Spacing>
      <SyntaxHighlighter language="go" value={GoHttpServer} />
    </Spacing>
  ))
  .add("Javascript", () => (
    <Spacing>
      <SyntaxHighlighter language="javascript" value={JsHttpServer} />
    </Spacing>
  ))
  .add("Python", () => (
    <Spacing>
      <SyntaxHighlighter language="python" value={PyHttpServer} />
    </Spacing>
  ))
  .add("Markdown", () => (
    <Spacing>
      <SyntaxHighlighter language="markdown" value={Markdown} />
    </Spacing>
  ))
  .add("Without line numbers", () => (
    <Spacing>
      <SyntaxHighlighter language="java" value={JavaHttpServer} showLineNumbers={false} />
    </Spacing>
  ));
