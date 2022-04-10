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

import React, { FC, useEffect, useState } from "react";
import SyntaxHighlighter, { Props } from "./SyntaxHighlighter";
import { ComponentMeta, ComponentStory } from "@storybook/react";

import HttpServerGo from "./__resources__/HttpServer.go";
import HttpServerJava from "./__resources__/HttpServer.java";
import HttpServerJs from "./__resources__/HttpServer.js";
import HttpServerPy from "./__resources__/HttpServer.py";

export default {
  title: "SyntaxHighlighter",
  component: SyntaxHighlighter,
  parameters: {
    layout: "fullscreen",
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
