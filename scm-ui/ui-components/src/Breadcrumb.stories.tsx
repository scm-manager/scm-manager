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
import Breadcrumb from "./Breadcrumb";
import repository from "./__resources__/repository";
// @ts-ignore ignore unknown png
import Git from "./__resources__/git-logo.png";
import { MemoryRouter } from "react-router-dom";
import Icon from "./Icon";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 800px;
`;

const master = { name: "master", revision: "1", defaultBranch: true, _links: {} };
const path = "src/main/java/com/cloudogu";
const longPath =
  "dream-path/src/main/scm-plugins/javaUtilityHomeHousingLinkReferrer/sonia/scm/repositoryUndergroundSupportManager/spi/SvnRepositoryServiceResolver.java";
const baseUrl = "scm-manager.org/scm/repo/hitchhiker/heartOfGold/sources";
const sources = Git;
const prefix = (
  <a href="#link">
    <Icon name="heart" color="danger" alt="heart icon" />
  </a>
);

storiesOf("BreadCrumb", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => (
    <Breadcrumb
      repository={repository}
      defaultBranch={master}
      branch={master}
      path={path}
      baseUrl={baseUrl}
      sources={sources}
      revision={"1"}
      permalink={"/" + path}
    />
  ))
  .add("Long path", () => (
    <Breadcrumb
      repository={repository}
      defaultBranch={master}
      branch={master}
      path={longPath}
      baseUrl={baseUrl}
      sources={sources}
      revision={"1"}
      permalink={"/" + longPath}
    />
  ))
  .add("With prefix button", () => (
    <Breadcrumb
      repository={repository}
      defaultBranch={master}
      branch={master}
      path={path}
      baseUrl={baseUrl}
      sources={sources}
      revision={"1"}
      permalink={"/" + path}
      preButtons={prefix}
    />
  ))
  .add("Not clickable", () => (
    <Breadcrumb
      repository={repository}
      defaultBranch={master}
      branch={master}
      path={path}
      baseUrl={baseUrl}
      sources={sources}
      revision={"1"}
      permalink={"/" + path}
      clickable={false}
    />
  ));
