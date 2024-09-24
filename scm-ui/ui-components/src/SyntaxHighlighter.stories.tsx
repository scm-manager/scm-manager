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
