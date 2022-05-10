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
import { one, two, three, four, five } from "../../__resources__/changesets";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
// @ts-ignore Import unknown file type
import hitchhiker from "../../__resources__/hitchhiker.png";
import { Person } from "../../avatar/Avatar";
import { Changeset } from "@scm-manager/ui-types";
import { Replacement } from "@scm-manager/ui-text";

const Wrapper = styled.div`
  margin: 25rem 4rem;
`;

const robohash = (person: Person) => {
  return `https://robohash.org/${person.mail}`;
};

const withAvatarFactory = (factory: (person: Person) => string, changeset: Changeset) => {
  const binder = new Binder("changeset stories");
  binder.bind("avatar.factory", factory);
  return (
    <BinderContext.Provider value={binder}>
      <ChangesetRow repository={repository} changeset={changeset} />
    </BinderContext.Provider>
  );
};

const withReplacements = (
  replacements: ((changeset: Changeset, value: string) => Replacement[])[],
  changeset: Changeset
) => {
  const binder = new Binder("changeset stories");
  replacements.forEach((replacement) => binder.bind("changeset.description.tokens", replacement));
  return (
    <BinderContext.Provider value={binder}>
      <ChangesetRow repository={repository} changeset={changeset} />
    </BinderContext.Provider>
  );
};

function copy<T>(input: T): T {
  return JSON.parse(JSON.stringify(input));
}

storiesOf("Repositories/Changesets", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator((storyFn) => <Wrapper className="box box-link-shadow">{storyFn()}</Wrapper>)
  .add("Default", () => <ChangesetRow repository={repository} changeset={three} />)
  .add("With Committer", () => <ChangesetRow repository={repository} changeset={two} />)
  .add("With Committer and Co-Author", () => <ChangesetRow repository={repository} changeset={one} />)
  .add("With multiple Co-Authors", () => <ChangesetRow repository={repository} changeset={four} />)
  .add("With avatar", () => {
    return withAvatarFactory(() => hitchhiker, three);
  })
  .add("Commiter and Co-Authors with avatar", () => {
    return withAvatarFactory(robohash, one);
  })
  .add("Co-Authors with avatar", () => {
    return withAvatarFactory(robohash, four);
  })
  .add("Replacements", () => {
    const link = <a href={"http://example.com/hog"}>HOG-42</a>;
    const mail = <a href={"mailto:hog@example.com"}>Arthur</a>;
    return withReplacements(
      [
        () => [{ textToReplace: "HOG-42", replacement: link }],
        () => [{ textToReplace: "arthur@guide.galaxy", replacement: mail }],
      ],
      five
    );
  })
  .add("With unknown signature", () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "NOT_FOUND",
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  })
  .add("With valid signature", () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "VERIFIED",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  })
  .add("With unowned signature", () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "VERIFIED",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  })
  .add("With contactless signature", () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "VERIFIED",
        owner: "trillian",
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  })
  .add("With invalid signature", () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "INVALID",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  })
  .add("With multiple signatures and invalid status", () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x912389FJIQW8W223",
        type: "gpg",
        status: "INVALID",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "VERIFIED",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
      {
        keyId: "0x9123891239VFIA33",
        type: "gpg",
        status: "NOT_FOUND",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  })
  .add("With multiple signatures and valid status", () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x912389FJIQW8W223",
        type: "gpg",
        status: "NOT_FOUND",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "VERIFIED",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
      {
        keyId: "0x9123891239VFIA33",
        type: "gpg",
        status: "NOT_FOUND",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  })
  .add("With multiple signatures and not found status", () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x912389FJIQW8W223",
        type: "gpg",
        status: "NOT_FOUND",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
      {
        keyId: "0x9123891239VFIA33",
        type: "gpg",
        status: "NOT_FOUND",
        owner: "trillian",
        contacts: [
          {
            name: "Tricia Marie McMilla",
            mail: "trillian@hitchhiker.com",
          },
        ],
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  });
