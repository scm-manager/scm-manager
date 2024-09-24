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

import React, { FC, Fragment, useEffect, useState } from "react";
import SyntaxHighlighter, { Props } from "./SyntaxHighlighter";
import { ComponentMeta, ComponentStory } from "@storybook/react";

import HttpServerGo from "./__resources__/HttpServer.go";
import HttpServerJava from "./__resources__/HttpServer.java";
import HttpServerJs from "./__resources__/HttpServer.js";
import HttpServerPy from "./__resources__/HttpServer.py";
import HttpServerWithMarkerJs from "./__resources__/HttpServerWithMarker.js";
import LogbackXml from "./__resources__/Logback.xml";
import ReadmeMd from "./__resources__/Readme.md";

export default {
  title: "SyntaxHighlighter",
  component: SyntaxHighlighter,
  argTypes: {
    lineWrapper: {
      table: {
        disable: true,
      },
    },
    markerConfig: {
      table: {
        disable: true,
      },
    },
    renderer: {
      table: {
        disable: true,
      },
    },
  },
} as ComponentMeta<typeof SyntaxHighlighter>;

const Template: ComponentStory<typeof SyntaxHighlighter> = (args: Props) => <SyntaxHighlighter {...args} />;

export const Go = Template.bind({});
Go.args = {
  language: "go",
  value: HttpServerGo,
};

export const Java = Template.bind({});
Java.args = {
  language: "java",
  value: HttpServerJava,
};

export const JavaScript = Template.bind({});
JavaScript.args = {
  language: "javascript",
  value: HttpServerJs,
};

export const Python = Template.bind({});
Python.args = {
  language: "python",
  value: HttpServerPy,
};

const LineWrapper: FC<{ lineNumber: number }> = ({ lineNumber, children }) => (
  <div>
    <span style={{ width: "2rem", display: "inline-block" }}>{lineNumber}</span>
    {children}
  </div>
);

export const LineNumbers = Template.bind({});
LineNumbers.args = {
  language: "go",
  value: HttpServerGo,
  lineWrapper: LineWrapper,
};

export const LargeCss: ComponentStory<typeof SyntaxHighlighter> = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [content, setContent] = useState<string>();
  useEffect(() => {
    setIsLoading(true);
    fetch("/large.css")
      .then((r) => r.text())
      .then((text) => setContent(text))
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading || !content) {
    return <p>Loading ...</p>;
  }

  return <SyntaxHighlighter language="css" value={content} lineWrapper={LineWrapper} />;
};

export const HugeCss: ComponentStory<typeof SyntaxHighlighter> = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [content, setContent] = useState<string>();
  useEffect(() => {
    setIsLoading(true);
    fetch("/next.css")
      .then((r) => r.text())
      .then((text) => setContent(text))
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading || !content) {
    return <p>Loading ...</p>;
  }

  return <SyntaxHighlighter language="css" value={content} lineWrapper={LineWrapper} />;
};

const Mark: FC = ({ children }) => <mark>{children}</mark>;

const markerConfig = {
  start: "<|[[--",
  end: "--]]|>",
  wrapper: Mark,
};

export const Marker = Template.bind({});
Marker.args = {
  language: "javascript",
  value: HttpServerWithMarkerJs,
  markerConfig,
};

export const MarkerWithLineNumbers = Template.bind({});
MarkerWithLineNumbers.args = {
  language: "javascript",
  value: HttpServerWithMarkerJs,
  lineWrapper: LineWrapper,
  markerConfig,
};

export const MarkerWithLineNumbersAndLimit = Template.bind({});
MarkerWithLineNumbersAndLimit.args = {
  language: "javascript",
  value: HttpServerWithMarkerJs,
  nodeLimit: 10,
  lineWrapper: LineWrapper,
  markerConfig: {
    start: "<|[[--",
    end: "--]]|>",
    wrapper: Mark,
  },
};

export const WithRenderer = Template.bind({});
WithRenderer.args = {
  language: "javascript",
  value: HttpServerJs,
  renderer: ({ children }) => (
    <table>
      <tbody>
        {children.map((child, i) => (
          <tr key={i}>
            <td style={{ border: "1px solid black" }}>{i + 1}</td>
            <td style={{ border: "1px solid black" }}>{child}</td>
          </tr>
        ))}
      </tbody>
    </table>
  ),
};

export const WithRendererWithLimit = Template.bind({});
WithRendererWithLimit.args = {
  language: "javascript",
  value: HttpServerJs,
  nodeLimit: 10,
  renderer: ({ children }) => (
    <table style={{ border: 1 }}>
      <tbody>
        {children.map((child, i) => (
          <tr key={i}>
            <td style={{ border: "1px solid black" }}>{i + 1}</td>
            <td style={{ border: "1px solid black" }}>{child}</td>
          </tr>
        ))}
      </tbody>
    </table>
  ),
};

export const XmlAsMarkup = Template.bind({});
XmlAsMarkup.args = {
  language: "xml",
  value: LogbackXml,
};

export const RenderAsFragment: ComponentStory<typeof SyntaxHighlighter> = () => (
  <>
    <h2>This is rendered as fragment without code or pre tags</h2>
    <SyntaxHighlighter value={HttpServerGo} language="go" as={Fragment} />
  </>
);

export const MarkdownWithFrontmatter = Template.bind({});
MarkdownWithFrontmatter.args = {
  language: "markdown",
  value: ReadmeMd,
};
