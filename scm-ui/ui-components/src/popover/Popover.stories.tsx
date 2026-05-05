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
// import React from "react";
// import styled from "styled-components";
// import usePopover from "./usePopover";
// import Popover from "./Popover";
//
// const Wrapper = styled.div`
//   width: 100%;
//   margin: 20rem;
// `;
//
// storiesOf("Popover", module)
//   .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
//   .add("Default", () =>
//     React.createElement(() => {
//       const { triggerProps, popoverProps } = usePopover();
//
//       return (
//         <div>
//           <Popover title={<strong>Spaceship Heart of Gold</strong>} width={512} {...popoverProps}>
//             <p>
//               The Heart of Gold is the sleekest, most advanced, coolest spaceship in the galaxy. Its stunning good looks
//               mirror its awesome speed and power. It is powered by the revolutionary new Infinite Improbability Drive,
//               which lets the ship pass through every point in every universe simultaneously.
//             </p>
//           </Popover>
//           <button className="button" {...triggerProps}>
//             Trigger
//           </button>
//         </div>
//       );
//     })
//   )
//   .add("Link", () =>
//     React.createElement(() => {
//       const { triggerProps, popoverProps } = usePopover();
//
//       return (
//         <div>
//           <Popover title={<strong>Spaceship Heart of Gold</strong>} width={512} {...popoverProps}>
//             <p>
//               The Heart of Gold is the sleekest, most advanced, coolest spaceship in the galaxy. Its stunning good looks
//               mirror its awesome speed and power. It is powered by the revolutionary new Infinite Improbability Drive,
//               which lets the ship pass through every point in every universe simultaneously.
//             </p>
//           </Popover>
//           <button {...triggerProps}>Trigger</button>
//         </div>
//       );
//     })
//   );

import React from "react";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";

import Popover from "./Popover";
import usePopover from "./usePopover";

const Wrapper = styled.div`
  /* A large margin is used to ensure the popover has space to render in all directions */
  margin: 20rem;
  width: 100%;
`;

const popoverContent = (
  <p>
    The Heart of Gold is the sleekest, most advanced, coolest spaceship in the galaxy. Its stunning good looks mirror
    its awesome speed and power. It is powered by the revolutionary new Infinite Improbability Drive, which lets the
    ship pass through every point in every universe simultaneously.
  </p>
);

const meta: Meta<typeof Popover> = {
  title: "Components/Popover",
  component: Popover,
  decorators: [(Story) => <Wrapper>{Story()}</Wrapper>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: (args) => {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const { triggerProps, popoverProps } = usePopover();

    return (
      <div>
        <Popover title={<strong>Spaceship Heart of Gold</strong>} width={512} {...popoverProps}>
          {popoverContent}
        </Popover>
        {/* @ts-ignore */}
        <button className="button" {...triggerProps}>
          Trigger
        </button>
      </div>
    );
  },
};

export const WithLinkTrigger: Story = {
  name: "With Link as Trigger",
  render: () => {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const { triggerProps, popoverProps } = usePopover();
    return (
      <div>
        <Popover title={<strong>Spaceship Heart of Gold</strong>} width={512} {...popoverProps}>
          {popoverContent}
        </Popover>
        {/* @ts-ignore */}
        <a href="#" {...triggerProps}>
          Trigger
        </a>
      </div>
    );
  },
};
