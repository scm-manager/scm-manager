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

import { storiesOf } from "@storybook/react";
import * as React from "react";
import styled from "styled-components";
import Annotate, { AnnotatedSource } from "./Annotate";

const Wrapper = styled.div`
  margin: 2rem;
`;

const commitCreateNewApp = {
  revision: "0d8c1d328f4599b363755671afe667c7ace52bae",
  author: {
    name: "Arthur Dent",
    mail: "arthur.dent@hitchhiker.com"
  },
  description: "create new app",
  when: new Date()
};

const commitFixedMissingImport = {
  revision: "fab38559ce3ab8c388e067712b4bd7ab94b9fa9b",
  author: {
    name: "Tricia Marie McMillan",
    mail: "trillian@hitchhiker.com"
  },
  description: "fixed missing import",
  when: new Date()
};

const commitImplementMain = {
  revision: "5203292ab2bc0c020dd22adc4d3897da4930e43f",
  author: {
    name: "Ford Prefect",
    mail: "ford.prefect@hitchhiker.com"
  },
  description: "implemented main function",
  when: new Date()
};

const source: AnnotatedSource = {
  language: "go",
  lines: [
    {
      lineNumber: 1,
      code: "package main",
      ...commitCreateNewApp
    },
    {
      lineNumber: 2,
      code: "",
      ...commitCreateNewApp
    },
    {
      lineNumber: 3,
      code: 'import "fmt"',
      ...commitFixedMissingImport
    },
    {
      lineNumber: 4,
      code: "",
      ...commitFixedMissingImport
    },
    {
      lineNumber: 5,
      code: "func main() {",
      ...commitCreateNewApp
    },
    {
      lineNumber: 6,
      code: '  fmt.Println("Hello World")',
      ...commitImplementMain
    },
    {
      lineNumber: 7,
      code: "}",
      ...commitCreateNewApp
    },
    {
      lineNumber: 8,
      code: "",
      ...commitCreateNewApp
    }
  ]
};

storiesOf("Annotate", module)
  .addDecorator(storyFn => <Wrapper className="box box-link-shadow">{storyFn()}</Wrapper>)
  .add("Default", () => <Annotate source={source} />);
