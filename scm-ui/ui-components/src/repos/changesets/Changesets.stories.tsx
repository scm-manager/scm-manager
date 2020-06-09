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
import { MemoryRouter } from "react-router-dom";
import repository from "../../__resources__/repository";
import ChangesetRow from "./ChangesetRow";
import {one, two, three, four} from "../../__resources__/changesets";
import {Binder, BinderContext} from "@scm-manager/ui-extensions";
// @ts-ignore
import hitchhiker from "../../__resources__/hitchhiker.png";
import {Person} from "../../avatar/Avatar";
import {Changeset} from "@scm-manager/ui-types/src";

const Wrapper = styled.div`
  margin: 2rem;
`;

const robohash = (person: Person) => {
  return `https://robohash.org/${person.mail}`;
}

const withAvatarFactory = (factory: (person: Person) => string, changeset: Changeset) => {
  const binder = new Binder("changeset stories");
  binder.bind("avatar.factory", factory);
  return (
    <BinderContext.Provider value={binder}>
      <ChangesetRow repository={repository} changeset={changeset} />
    </BinderContext.Provider>
  );
};

storiesOf("Changesets", module)
  .addDecorator(story => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator(storyFn => <Wrapper className="box box-link-shadow">{storyFn()}</Wrapper>)
  .add("Default", () => (
    <ChangesetRow repository={repository} changeset={three} />
  ))
  .add("With Committer", () => (
    <ChangesetRow repository={repository} changeset={two} />
  ))
  .add("With Committer and Co-Author", () => (
    <ChangesetRow repository={repository} changeset={one} />
  ))
  .add("With multiple Co-Authors", () => (
    <ChangesetRow repository={repository} changeset={four} />
  ))
  .add("With avatar", () => {
    return withAvatarFactory(person => hitchhiker, three);
  })
  .add("Co-Authors with avatar", () => {
    return withAvatarFactory(robohash, four);
  });
