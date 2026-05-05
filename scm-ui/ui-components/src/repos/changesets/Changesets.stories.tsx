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

// import { storiesOf } from "@storybook/react";
// import * as React from "react";
// import styled from "styled-components";
// import { MemoryRouter } from "react-router-dom";
// import repository from "../../__resources__/repository";
// import ChangesetRow from "./ChangesetRow";
// import { five, four, one, three, two } from "../../__resources__/changesets";
// import { Binder, BinderContext } from "@scm-manager/ui-extensions";
// // @ts-ignore
// import hitchhiker from "../../__resources__/hitchhiker.png";
// import { Person } from "../../avatar/Avatar";
// import { Changeset } from "@scm-manager/ui-types";
// import { Replacement, ShortcutDocsContextProvider } from "@scm-manager/ui-core";
// import ChangesetList from "./ChangesetList";
//
// const Wrapper = styled.div`
//   margin: 25rem 4rem;
// `;
//
// const robohash = (person: Person) => {
//   return `https://robohash.org/${person.mail}`;
// };
//
// const withAvatarFactory = (factory: (person: Person) => string, changeset: Changeset) => {
//   const binder = new Binder("changeset stories");
//   binder.bind("avatar.factory", factory);
//   return (
//     <BinderContext.Provider value={binder}>
//       <ChangesetRow repository={repository} changeset={changeset} />
//     </BinderContext.Provider>
//   );
// };
//
// const withReplacements = (
//   replacements: ((changeset: Changeset, value: string) => Replacement[])[],
//   changeset: Changeset
// ) => {
//   const binder = new Binder("changeset stories");
//   replacements.forEach((replacement) => binder.bind("changeset.description.tokens", replacement));
//   return (
//     <BinderContext.Provider value={binder}>
//       <ChangesetRow repository={repository} changeset={changeset} />
//     </BinderContext.Provider>
//   );
// };
//
// function copy<T>(input: T): T {
//   return JSON.parse(JSON.stringify(input));
// }
//
// storiesOf("Repositories/Changesets", module)
//   .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
//   .addDecorator((storyFn) => <Wrapper className="box box-link-shadow">{storyFn()}</Wrapper>)
//   .add("Default", () => <ChangesetRow repository={repository} changeset={three} />)
//   .add("With Committer", () => <ChangesetRow repository={repository} changeset={two} />)
//   .add("With Committer and Co-Author", () => <ChangesetRow repository={repository} changeset={one} />)
//   .add("With multiple Co-Authors", () => <ChangesetRow repository={repository} changeset={four} />)
//   .add("With avatar", () => {
//     return withAvatarFactory(() => hitchhiker, three);
//   })
//   .add("Commiter and Co-Authors with avatar", () => {
//     return withAvatarFactory(robohash, one);
//   })
//   .add("Co-Authors with avatar", () => {
//     return withAvatarFactory(robohash, four);
//   })
//   .add("Replacements", () => {
//     const link = <a href="http://example.com/hog">HOG-42</a>;
//     const mail = <a href="mailto:hog@example.com">Arthur</a>;
//     return withReplacements(
//       [
//         () => [{ textToReplace: "HOG-42", replacement: link }],
//         () => [{ textToReplace: "arthur@guide.galaxy", replacement: mail }],
//       ],
//       five
//     );
//   })
//   .add("With unknown signature", () => {
//     const changeset = copy(three);
//     changeset.signatures = [
//       {
//         keyId: "0x247E908C6FD35473",
//         type: "gpg",
//         status: "NOT_FOUND",
//       },
//     ];
//     return <ChangesetRow repository={repository} changeset={changeset} />;
//   })
//   .add("With valid signature", () => {
//     const changeset = copy(three);
//     changeset.signatures = [
//       {
//         keyId: "0x247E908C6FD35473",
//         type: "gpg",
//         status: "VERIFIED",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//     ];
//     return <ChangesetRow repository={repository} changeset={changeset} />;
//   })
//   .add("With unowned signature", () => {
//     const changeset = copy(three);
//     changeset.signatures = [
//       {
//         keyId: "0x247E908C6FD35473",
//         type: "gpg",
//         status: "VERIFIED",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//     ];
//     return <ChangesetRow repository={repository} changeset={changeset} />;
//   })
//   .add("With contactless signature", () => {
//     const changeset = copy(three);
//     changeset.signatures = [
//       {
//         keyId: "0x247E908C6FD35473",
//         type: "gpg",
//         status: "VERIFIED",
//         owner: "trillian",
//       },
//     ];
//     return <ChangesetRow repository={repository} changeset={changeset} />;
//   })
//   .add("With invalid signature", () => {
//     const changeset = copy(three);
//     changeset.signatures = [
//       {
//         keyId: "0x247E908C6FD35473",
//         type: "gpg",
//         status: "INVALID",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//     ];
//     return <ChangesetRow repository={repository} changeset={changeset} />;
//   })
//   .add("With multiple signatures and invalid status", () => {
//     const changeset = copy(three);
//     changeset.signatures = [
//       {
//         keyId: "0x912389FJIQW8W223",
//         type: "gpg",
//         status: "INVALID",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//       {
//         keyId: "0x247E908C6FD35473",
//         type: "gpg",
//         status: "VERIFIED",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//       {
//         keyId: "0x9123891239VFIA33",
//         type: "gpg",
//         status: "NOT_FOUND",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//     ];
//     return <ChangesetRow repository={repository} changeset={changeset} />;
//   })
//   .add("With multiple signatures and valid status", () => {
//     const changeset = copy(three);
//     changeset.signatures = [
//       {
//         keyId: "0x912389FJIQW8W223",
//         type: "gpg",
//         status: "NOT_FOUND",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//       {
//         keyId: "0x247E908C6FD35473",
//         type: "gpg",
//         status: "VERIFIED",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//       {
//         keyId: "0x9123891239VFIA33",
//         type: "gpg",
//         status: "NOT_FOUND",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//     ];
//     return <ChangesetRow repository={repository} changeset={changeset} />;
//   })
//   .add("With multiple signatures and not found status", () => {
//     const changeset = copy(three);
//     changeset.signatures = [
//       {
//         keyId: "0x912389FJIQW8W223",
//         type: "gpg",
//         status: "NOT_FOUND",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//       {
//         keyId: "0x9123891239VFIA33",
//         type: "gpg",
//         status: "NOT_FOUND",
//         owner: "trillian",
//         contacts: [
//           {
//             name: "Tricia Marie McMilla",
//             mail: "trillian@hitchhiker.com",
//           },
//         ],
//       },
//     ];
//     return <ChangesetRow repository={repository} changeset={changeset} />;
//   })
//   .add("List with navigation", () => (
//     <ShortcutDocsContextProvider>
//       <ChangesetList repository={repository} changesets={[copy(one), copy(two), copy(three)]} />
//     </ShortcutDocsContextProvider>
//   ));

import React from "react";
import { MemoryRouter } from "react-router-dom";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";
import { Replacement, ShortcutDocsContextProvider } from "@scm-manager/ui-core";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
import { Changeset } from "@scm-manager/ui-types";
import repository from "../../__resources__/repository";
import { five, four, one, three, two } from "../../__resources__/changesets";
// @ts-ignore
import hitchhiker from "../../__resources__/hitchhiker.png";
import { Person } from "../../avatar/Avatar";
import ChangesetRow from "./ChangesetRow";
import ChangesetList from "./ChangesetList";

const Wrapper = styled.div`
  margin: 2rem 4rem;
`;

const robohash = (person: Person) => `https://robohash.org/${person.mail}`;

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
  changeset: Changeset,
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

const meta: Meta<typeof ChangesetRow> = {
  title: "Repositories/Changesets",
  component: ChangesetRow,
  decorators: [
    (Story) => <MemoryRouter initialEntries={["/"]}>{Story()}</MemoryRouter>,
    (Story) => <Wrapper className="box box-link-shadow">{Story()}</Wrapper>,
  ],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: { repository, changeset: three },
};

export const WithCommitter: Story = {
  args: { repository, changeset: two },
};

export const WithCommitterAndCoAuthor: Story = {
  args: { repository, changeset: one },
};

export const WithMultipleCoAuthors: Story = {
  args: { repository, changeset: four },
};

export const WithAvatar: Story = {
  render: () => withAvatarFactory(() => hitchhiker, three),
};

export const CommitterAndCoAuthorsWithAvatar: Story = {
  render: () => withAvatarFactory(robohash, one),
};

export const CoAuthorsWithAvatar: Story = {
  render: () => withAvatarFactory(robohash, four),
};

export const Replacements: Story = {
  render: () => {
    const link = <a href="http://example.com/hog">HOG-42</a>;
    const mail = <a href="mailto:hog@example.com">Arthur</a>;
    return withReplacements(
      [
        () => [{ textToReplace: "HOG-42", replacement: link }],
        () => [{ textToReplace: "arthur@guide.galaxy", replacement: mail }],
      ],
      five,
    );
  },
};

export const WithUnknownSignature: Story = {
  render: () => {
    const changeset = copy(three);
    changeset.signatures = [{ keyId: "0x247E908C6FD35473", type: "gpg", status: "NOT_FOUND" }];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  },
};

export const WithValidSignature: Story = {
  render: () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "VERIFIED",
        owner: "trillian",
        contacts: [{ name: "Tricia Marie McMilla", mail: "trillian@hitchhiker.com" }],
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  },
};

export const WithInvalidSignature: Story = {
  render: () => {
    const changeset = copy(three);
    changeset.signatures = [
      {
        keyId: "0x247E908C6FD35473",
        type: "gpg",
        status: "INVALID",
        owner: "trillian",
        contacts: [{ name: "Tricia Marie McMilla", mail: "trillian@hitchhiker.com" }],
      },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  },
};

export const WithMultipleSignaturesInvalid: Story = {
  name: "With Multiple Signatures (Invalid Status)",
  render: () => {
    const changeset = copy(three);
    changeset.signatures = [
      { keyId: "0x912389FJIQW8W223", type: "gpg", status: "INVALID", owner: "trillian" },
      { keyId: "0x247E908C6FD35473", type: "gpg", status: "VERIFIED", owner: "trillian" },
      { keyId: "0x9123891239VFIA33", type: "gpg", status: "NOT_FOUND", owner: "trillian" },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  },
};

export const WithMultipleSignaturesValid: Story = {
  name: "With Multiple Signatures (Valid Status)",
  render: () => {
    const changeset = copy(three);
    changeset.signatures = [
      { keyId: "0x912389FJIQW8W223", type: "gpg", status: "NOT_FOUND", owner: "trillian" },
      { keyId: "0x247E908C6FD35473", type: "gpg", status: "VERIFIED", owner: "trillian" },
      { keyId: "0x9123891239VFIA33", type: "gpg", status: "NOT_FOUND", owner: "trillian" },
    ];
    return <ChangesetRow repository={repository} changeset={changeset} />;
  },
};

export const ListWithNavigation: Story = {
  name: "List With Navigation",
  render: () => (
    <ShortcutDocsContextProvider>
      <ChangesetList repository={repository} changesets={[copy(one), copy(two), copy(three)]} />
    </ShortcutDocsContextProvider>
  ),
};
