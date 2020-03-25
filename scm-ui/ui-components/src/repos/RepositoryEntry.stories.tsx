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
import React from "react";
import styled from "styled-components";
import GitRepository from "../__resources__/Git-Repository";
import HgRepository from "../__resources__/Hg-Repository";
// @ts-ignore ignore unknown png
import Git from "../__resources__/git-logo.png";
// @ts-ignore ignore unknown png
import Hg from "../__resources__/hg-logo.png";
import RepositoryEntry from "./RepositoryEntry";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
import { Repository } from "@scm-manager/ui-types";

const Container = styled.div`
  padding: 2rem 6rem;
`;

const bindAvatar = (binder: Binder, avatar: string) => {
  binder.bind("repos.repository-avatar", () => {
    return avatar;
  });
};

const bindBeforeTitle = (binder: Binder, beforeTitle: string) => {
  binder.bind("repository.card.beforeTitle", () => {
    return beforeTitle;
  });
};

const withBinder = (binder: Binder, repository: Repository) => {
  return (
    <BinderContext.Provider value={binder}>
      <RepositoryEntry repository={repository} />
    </BinderContext.Provider>
  );
};

storiesOf("RepositoryEntry", module)
  .addDecorator(storyFn => <Container>{storyFn()}</Container>)
  .add("Git-Repo", () => {
    const binder = new Binder("git-story");
    bindAvatar(binder, "Git");
    return withBinder(binder, GitRepository);
  })
  .add("Hg-Repo", () => {
    const binder = new Binder("hg-story");
    bindBeforeTitle(binder, "Hg");
    return withBinder(binder, HgRepository);
  });
