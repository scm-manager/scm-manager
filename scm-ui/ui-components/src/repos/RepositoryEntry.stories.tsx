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
import React, { FC, ReactNode } from "react";
import styled from "styled-components";
import repository from "../__resources__/repository";
// @ts-ignore ignore unknown png
import Git from "../__resources__/git-logo.png";
import RepositoryEntry from "./RepositoryEntry";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
import { Repository } from "@scm-manager/ui-types";
import Image from "../Image";
import { MemoryRouter } from "react-router-dom";
import { Color } from "../styleConstants";
import RepositoryFlag from "./RepositoryFlag";

const baseDate = "2020-03-26T12:13:42+02:00";

const Spacing = styled.div`
  margin: 2rem;
`;

const Container: FC = ({ children }) => <Spacing>{children}</Spacing>;

const bindAvatar = (binder: Binder, avatar: string) => {
  binder.bind("repos.repository-avatar", () => {
    return <Image src={avatar} alt="Logo" />;
  });
};

const bindFlag = (binder: Binder, color: Color, label: string) => {
  binder.bind("repository.card.flags", () => (
    <RepositoryFlag title={label} color={color} tooltipLocation="right">
      {label}
    </RepositoryFlag>
  ));
};

const bindBeforeTitle = (binder: Binder, extension: ReactNode) => {
  binder.bind("repository.card.beforeTitle", () => {
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

const archivedRepository = { ...repository, archived: true };
const exportingRepository = { ...repository, exporting: true };
const longTextRepository = {
  ...repository,
  name: "veeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeery-loooooooooooooooooooooooooooooooooooooooooooooooooooong-repooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo-naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaame",
  description:
    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
};
const healthCheckFailedRepository = {
  ...repository,
  healthCheckFailures: [
    {
      id: "4211",
      summary: "Something failed",
      description: "Something realy bad happend",
      url: "https://something-realy-bad.happend",
    },
  ],
};
const archivedExportingRepository = {
  ...repository,
  archived: true,
  exporting: true,
  description:
    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.",
};

storiesOf("Repositories/RepositoryEntry", module)
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
  })
  .add("With long texts", () => {
    return <RepositoryEntry repository={longTextRepository} baseDate={baseDate} />;
  });
