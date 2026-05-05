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

// import styled from "styled-components";
// import * as React from "react";
// import { ReactNode, useState } from "react";
// import { MemoryRouter } from "react-router-dom";
// import { storiesOf } from "@storybook/react";
// import Notification from "./Notification";
//
// const Wrapper = styled.div`
//   margin: 2rem;
//   max-width: 400px;
// `;
//
// const content =
//   "Cleverness nuclear genuine static irresponsibility invited President Zaphod\n" +
//   "Beeblebrox hyperspace ship. Another custard through computer-generated universe\n" +
//   "shapes field strong disaster parties Russell’s ancestors infinite colour\n" +
//   "imaginative generator sweep.";
//
// const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;
//
// storiesOf("Notification", module)
//   .addDecorator(RoutingDecorator)
//   .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
//   .add("Primary", () => <Notification type="primary">{content}</Notification>)
//   .add("Success", () => <Notification type="success">{content}</Notification>)
//   .add("Info", () => <Notification type="info">{content}</Notification>)
//   .add("Warning", () => <Notification type="warning">{content}</Notification>)
//   .add("Danger", () => <Notification type="danger">{content}</Notification>)
//   .add("Closeable", () => <Closeable />);
//
// const Closeable = () => {
//   const [show, setShow] = useState(true);
//
//   const hide = () => {
//     setShow(false);
//   };
//
//   if (!show) {
//     return null;
//   }
//
//   return (
//     <Notification type="warning" onClose={() => hide()}>
//       {content}
//     </Notification>
//   );
// };

import React, { ReactNode, useState } from "react";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";

import Notification from "./Notification";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const content =
  "Cleverness nuclear genuine static irresponsibility invited President Zaphod\n" +
  "Beeblebrox hyperspace ship. Another custard through computer-generated universe\n" +
  "shapes field strong disaster parties Russell’s ancestors infinite colour\n" +
  "imaginative generator sweep.";

const CloseableExample = () => {
  const [show, setShow] = useState(true);

  if (!show) {
    return <p>Notification has been closed.</p>;
  }

  return (
    <Notification type="warning" onClose={() => setShow(false)}>
      {content}
    </Notification>
  );
};

const meta: Meta<typeof Notification> = {
  title: "Components/Notification",
  component: Notification,
  decorators: [(Story) => <Wrapper>{Story()}</Wrapper>],
  argTypes: {
    type: {
      control: "select",
      options: ["primary", "success", "info", "warning", "danger"],
    },
    onClose: { action: "closed" },
  },
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Primary: Story = {
  args: {
    type: "primary",
    children: content,
  },
};

export const Success: Story = {
  args: {
    ...Primary.args, // Reuses args from Primary
    type: "success",
  },
};

export const Info: Story = {
  args: {
    ...Primary.args,
    type: "info",
  },
};

export const Warning: Story = {
  args: {
    ...Primary.args,
    type: "warning",
  },
};

export const Danger: Story = {
  args: {
    ...Primary.args,
    type: "danger",
  },
};

export const Closeable: Story = {
  render: () => <CloseableExample />,
};
