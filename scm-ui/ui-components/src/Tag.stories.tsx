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
// import { storiesOf } from "@storybook/react";
// import React, { ReactNode } from "react";
// import Tag from "./Tag";
// import { MemoryRouter } from "react-router-dom";
// import { Color, colors, sizes } from "./styleConstants";
//
// const Wrapper = styled.div`
//   margin: 2rem;
//   max-width: 400px;
// `;
//
// const Spacing = styled.div`
//   padding: 0.5rem;
// `;
//
// const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;
//
// storiesOf("Tag", module)
//   .addDecorator(RoutingDecorator)
//   .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
//   .add("Default", () => <Tag label="Default tag" />)
//   .add("Rounded", () => <Tag label="Rounded tag" color="dark" rounded={true} />)
//   .add("With Icon", () => <Tag label="System" icon="bolt" />)
//   .add("Colors", () => (
//     <div>
//       {colors.map((color) => (
//         <Spacing key={color}>
//           <Tag color={color} label={color} />
//         </Spacing>
//       ))}
//     </div>
//   ))
//   .add("Outlined", () => (
//     <div>
//       {(["success", "black", "danger"] as Color[]).map((color) => (
//         <Spacing key={color}>
//           <Tag color={color} label={color} outlined={true} />
//         </Spacing>
//       ))}
//     </div>
//   ))
//   .add("With title", () => <Tag label="hover me" title="good job" />)
//   .add("Clickable", () => <Tag label="Click here" onClick={() => alert("Not so fast")} />)
//   .add("Sizes", () => (
//     <div>
//       {sizes.map((size) => (
//         <Spacing key={size}>
//           <Tag size={size} label={size} />
//         </Spacing>
//       ))}
//     </div>
//   ));

import React, { ReactNode } from "react";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";

import Tag from "./Tag";
import { Color, colors, sizes } from "./styleConstants";

// --- Helper Components & Styles ---

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const Spacing = styled.div`
  padding: 0.5rem;
  display: inline-block; // Ensures spacing works for multiple tags in a row
`;

// --- Storybook Metadata ---

const meta: Meta<typeof Tag> = {
  title: "Components/Tag",
  component: Tag,
  decorators: [(Story) => <Wrapper>{Story()}</Wrapper>],
  argTypes: {
    onClick: { action: "clicked" },
    color: {
      control: "select",
      options: colors,
    },
    size: {
      control: "select",
      options: sizes,
    },
  },
  tags: ["autodocs"],
};

export default meta;

// --- Story Definitions ---

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    label: "Default tag",
  },
};

export const Rounded: Story = {
  args: {
    label: "Rounded tag",
    color: "dark",
    rounded: true,
  },
};

export const WithIcon: Story = {
  args: {
    label: "System",
    icon: "bolt",
  },
};

export const WithTitle: Story = {
  args: {
    label: "hover me",
    title: "good job",
  },
};

export const Clickable: Story = {
  args: {
    label: "Click here",
    // The onClick prop is automatically handled by argTypes
  },
};

// For stories that showcase multiple variations, we use a custom `render` function.
export const Colors: Story = {
  render: () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Tag color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ),
};

export const Outlined: Story = {
  render: () => (
    <div>
      {(["success", "black", "danger"] as Color[]).map((color) => (
        <Spacing key={color}>
          <Tag color={color} label={color} outlined={true} />
        </Spacing>
      ))}
    </div>
  ),
};

export const Sizes: Story = {
  render: () => (
    <div>
      {sizes.map((size) => (
        <Spacing key={size}>
          <Tag size={size} label={size} />
        </Spacing>
      ))}
    </div>
  ),
};
