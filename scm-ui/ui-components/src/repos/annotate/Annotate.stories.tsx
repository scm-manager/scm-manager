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

import { storiesOf } from "@storybook/react";
import React, { FC } from "react";
import styled from "styled-components";
import Annotate from "./Annotate";
import { MemoryRouter } from "react-router-dom";
import repository from "../../__resources__/repository";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
import { Person } from "../../avatar/Avatar";
import { AnnotatedSource } from "@scm-manager/ui-types";

const Wrapper = styled.div`
  margin: 2rem;
`;

const commitCreateNewApp = {
  revision: "0d8c1d328f4599b363755671afe667c7ace52bae",
  author: {
    name: "Arthur Dent",
    mail: "arthur.dent@hitchhiker.com",
  },
  description: "create new app",
  when: new Date("2020-04-09T13:07:42Z"),
};

const commitFixedMissingImport = {
  revision: "fab38559ce3ab8c388e067712b4bd7ab94b9fa9b",
  author: {
    name: "Tricia Marie McMillan",
    mail: "trillian@hitchhiker.com",
  },
  description: "fixed missing import",
  when: new Date("2020-05-10T09:18:42Z"),
};

const commitImplementMain = {
  revision: "5203292ab2bc0c020dd22adc4d3897da4930e43f",
  author: {
    name: "Ford Prefect",
    mail: "ford.prefect@hitchhiker.com",
  },
  description: "implemented main function",
  when: new Date("2020-04-12T16:29:42Z"),
};

const source: AnnotatedSource = {
  language: "go",
  lines: [
    {
      lineNumber: 1,
      code: "package main",
      ...commitCreateNewApp,
    },
    {
      lineNumber: 2,
      code: "",
      ...commitCreateNewApp,
    },
    {
      lineNumber: 3,
      code: 'import "fmt"',
      ...commitFixedMissingImport,
    },
    {
      lineNumber: 4,
      code: "",
      ...commitFixedMissingImport,
    },
    {
      lineNumber: 5,
      code: "func main() {",
      ...commitCreateNewApp,
    },
    {
      lineNumber: 6,
      code: '  fmt.Println("Hello World")',
      ...commitImplementMain,
    },
    {
      lineNumber: 7,
      code: "}",
      ...commitCreateNewApp,
    },
    {
      lineNumber: 8,
      code: "",
      ...commitCreateNewApp,
    },
  ],
};

const markdownSource: AnnotatedSource = {
  language: "markdown",
  lines: [
    {
      lineNumber: 1,
      code: "# Title",
      ...commitCreateNewApp,
    },
    {
      lineNumber: 2,
      code: "",
      ...commitCreateNewApp,
    },
    {
      lineNumber: 3,
      code: "This is a short Markdown text.",
      ...commitFixedMissingImport,
    },
    {
      lineNumber: 4,
      code: "",
      ...commitFixedMissingImport,
    },
    {
      lineNumber: 5,
      code: "With **bold** and __italic__ words.",
      ...commitCreateNewApp,
    },
    {
      lineNumber: 6,
      code: "",
      ...commitImplementMain,
    },
    {
      lineNumber: 7,
      code: "> This should be a quote",
      ...commitCreateNewApp,
    },
    {
      lineNumber: 8,
      code: "",
      ...commitCreateNewApp,
    },
  ],
};

const Robohash: FC = ({ children }) => {
  const binder = new Binder("robohash");
  binder.bind("avatar.factory", (person: Person) => `https://robohash.org/${person.mail}.png`);
  return <BinderContext.Provider value={binder}>{children}</BinderContext.Provider>;
};

storiesOf("Repositories/Annotate", module)
  .addDecorator((storyFn) => <MemoryRouter initialEntries={["/"]}>{storyFn()}</MemoryRouter>)
  .addDecorator((storyFn) => <Wrapper className="box">{storyFn()}</Wrapper>)
  .add("Default", () => (
    <Annotate source={source} repository={repository} baseDate={new Date("2020-04-16T09:22:42Z")} />
  ))
  .add("Markdown", () => (
    <Annotate source={markdownSource} repository={repository} baseDate={new Date("2020-04-15T09:47:42Z")} />
  ))
  .add("With Avatars", () => (
    <Robohash>
      <Annotate source={source} repository={repository} baseDate={new Date("2020-04-15T09:47:42Z")} />
    </Robohash>
  ));
