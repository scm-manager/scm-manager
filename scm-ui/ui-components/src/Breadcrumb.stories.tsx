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
  .addDecorator(story => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator(storyFn => <Wrapper>{storyFn()}</Wrapper>)
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
