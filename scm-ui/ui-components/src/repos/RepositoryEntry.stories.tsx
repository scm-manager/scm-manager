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
import React, { FC, ReactNode } from "react";
import styled from "styled-components";
import repository from "../__resources__/repository";
// @ts-ignore ignore unknown png
import Git from "../__resources__/git-logo.png";
import RepositoryEntry from "./RepositoryEntry";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
import { Repository } from "@scm-manager/ui-types";
import Image from "../Image";
import Icon from "../Icon";
import { MemoryRouter } from "react-router-dom";
import { Color } from "../styleConstants";
import RepositoryFlag from "./RepositoryFlag";

const baseDate = "2020-03-26T12:13:42+02:00";

const Spacing = styled.div`
  margin: 2rem;
`;

const Container: FC = ({ children }) => <Spacing className="box box-link-shadow">{children}</Spacing>;

const bindAvatar = (binder: Binder, avatar: string) => {
  binder.bind("repos.repository-avatar", () => {
    return <Image src={avatar} alt="Logo" />;
  });
};

const bindFlag = (binder: Binder, color: Color, label: string) => {
  binder.bind("repository.card.flags", () => (
    <RepositoryFlag title={label} color={color}>
      {label}
    </RepositoryFlag>
  ));
};

const bindBeforeTitle = (binder: Binder, extension: ReactNode) => {
  binder.bind("repository.card.beforeTitle", () => {
    return extension;
  });
};

const bindQuickLink = (binder: Binder, extension: ReactNode) => {
  binder.bind("repository.card.quickLink", () => {
    return extension;
  });
};

const withBinder = (binder: Binder, repo: Repository) => {
  return (
    <BinderContext.Provider value={binder}>
      <RepositoryEntry repository={repo} baseDate={baseDate} />
    </BinderContext.Provider>
  );
};

const QuickLink = (
  <a className="level-item">
    <Icon className="fa-lg" name="fas fa-code-branch fa-rotate-180 fa-fw" color="inherit" />
  </a>
);

const archivedRepository = { ...repository, archived: true };
const exportingRepository = { ...repository, exporting: true };
const healthCheckFailedRepository = {
  ...repository,
  healthCheckFailures: [
    {
      id: "4211",
      summary: "Something failed",
      description: "Something realy bad happend",
      url: "https://something-realy-bad.happend"
    }
  ]
};
const archivedExportingRepository = { ...repository, archived: true, exporting: true };

storiesOf("RepositoryEntry", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator((storyFn) => <Container>{storyFn()}</Container>)
  .add("Default", () => {
    return <RepositoryEntry repository={repository} baseDate={baseDate} />;
  })
  .add("Avatar EP", () => {
    const binder = new Binder("avatar");
    bindAvatar(binder, Git);
    return withBinder(binder, repository);
  })
  .add("Before Title EP", () => {
    const binder = new Binder("title");
    bindBeforeTitle(binder, <i className="far fa-star" />);
    return withBinder(binder, repository);
  })
  .add("Quick Link EP", () => {
    const binder = new Binder("title");
    bindQuickLink(binder, QuickLink);
    return withBinder(binder, repository);
  })
  .add("Archived", () => {
    const binder = new Binder("title");
    bindAvatar(binder, Git);
    return withBinder(binder, archivedRepository);
  })
  .add("Exporting", () => {
    const binder = new Binder("title");
    bindAvatar(binder, Git);
    return withBinder(binder, exportingRepository);
  })
  .add("HealthCheck Failure", () => {
    const binder = new Binder("title");
    bindAvatar(binder, Git);
    return withBinder(binder, healthCheckFailedRepository);
  })
  .add("RepositoryFlag EP", () => {
    const binder = new Binder("title");
    bindAvatar(binder, Git);
    bindFlag(binder, "success", "awesome");
    bindFlag(binder, "warning", "ouhhh...");
    return withBinder(binder, healthCheckFailedRepository);
  })
  .add("MultiRepositoryTags", () => {
    const binder = new Binder("title");
    bindAvatar(binder, Git);
    return withBinder(binder, archivedExportingRepository);
  });
