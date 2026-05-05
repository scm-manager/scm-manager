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

// import React from "react";
// import { MemoryRouter } from "react-router-dom";
// import { storiesOf } from "@storybook/react";
// import CardColumn from "./CardColumn";
// import Icon from "./Icon";
// import styled from "styled-components";
// import { DateFromNow } from ".";
// import repository from "./__resources__/repository";
//
// const Wrapper = styled.div`
//   margin: 2rem;
//   max-width: 400px;
// `;
//
// const link = "/foo/bar";
// const avatar = <Icon name="icons fa-2x fa-fw" alt="avatar" />;
// const title = <strong>title</strong>;
// const footerLeft = <small>left footer</small>;
// const footerRight = <small>right footer</small>;
// const baseDate = "2020-03-26T12:13:42+02:00";
//
// storiesOf("CardColumn", module)
//   .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
//   .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
//   .add("Default", () => (
//     <CardColumn
//       link={link}
//       avatar={avatar}
//       title={title}
//       description="A description can be added here."
//       footerLeft={footerLeft}
//       footerRight={footerRight}
//     />
//   ))
//   .add("Minimal", () => <CardColumn title={title} footerLeft={footerLeft} footerRight={footerRight} />)
//   .add("With hoverable date", () => (
//     <CardColumn
//       title={title}
//       footerLeft={footerLeft}
//       footerRight={
//         <small className="level-item">
//           <DateFromNow baseDate={baseDate} date={repository.creationDate} />
//         </small>
//       }
//     />
//   ));

import React from "react";
import { MemoryRouter } from "react-router-dom";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";

import CardColumn from "./CardColumn";
import Icon from "./Icon";
import { DateFromNow } from "@scm-manager/ui-core";
import repository from "./__resources__/repository";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const link = "/foo/bar";
const avatar = <Icon name="icons fa-2x fa-fw" alt="avatar" />;
const title = <strong>title</strong>;
const footerLeft = <small>left footer</small>;
const footerRight = <small>right footer</small>;
const baseDate = "2020-03-26T12:13:42+02:00";

const meta: Meta<typeof CardColumn> = {
  title: "Components/CardColumn",
  component: CardColumn,
  decorators: [
    (Story) => <MemoryRouter initialEntries={["/"]}>{Story()}</MemoryRouter>,
    (Story) => <Wrapper>{Story()}</Wrapper>,
  ],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    link: link,
    avatar: avatar,
    title: title,
    description: "A description can be added here.",
    footerLeft: footerLeft,
    footerRight: footerRight,
  },
};

export const Minimal: Story = {
  args: {
    title: title,
    footerLeft: footerLeft,
    footerRight: footerRight,
  },
};

export const WithHoverableDate: Story = {
  args: {
    title: title,
    footerLeft: footerLeft,
    footerRight: (
      <small className="level-item">
        <DateFromNow date={repository.creationDate} />
      </small>
    ),
  },
};
