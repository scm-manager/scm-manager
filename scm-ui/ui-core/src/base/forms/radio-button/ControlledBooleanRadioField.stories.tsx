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

import React, { FC } from "react";
import type { Meta, StoryObj } from "@storybook/react";

import Form from "../Form";
import FormRow from "../FormRow";
import ControlledBooleanRadioField from "./ControlledBooleanRadioField";

type WrapperProps = {
  defaultValue?: boolean;
  children: React.ReactNode;
};

/**
 * ControlledBooleanRadioField relies on the surrounding form context, so every story is wrapped in a Form.
 */
const FormWrapper: FC<WrapperProps> = ({ defaultValue = true, children }) => (
  <Form onSubmit={console.log} defaultValues={{ enabled: defaultValue }} translationPath={["sample", "form"]}>
    <FormRow>{children}</FormRow>
  </Form>
);

const meta: Meta<typeof ControlledBooleanRadioField> = {
  title: "Forms/ControlledBooleanRadioField",
  // @ts-ignore ControlledBooleanRadioField is a generic component
  component: ControlledBooleanRadioField,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "40rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  render: () => (
    <FormWrapper>
      <ControlledBooleanRadioField name="enabled" label="Enabled" trueLabel="Yes" falseLabel="No" />
    </FormWrapper>
  ),
};

export const WithDescription: Story = {
  render: () => (
    <FormWrapper>
      <ControlledBooleanRadioField
        name="enabled"
        label="Enabled"
        description="Controls whether the feature is turned on for this repository."
        trueLabel="Yes"
        falseLabel="No"
      />
    </FormWrapper>
  ),
};

export const FalseByDefault: Story = {
  render: () => (
    <FormWrapper defaultValue={false}>
      <ControlledBooleanRadioField name="enabled" label="Enabled" trueLabel="Yes" falseLabel="No" />
    </FormWrapper>
  ),
};

export const ReadOnly: Story = {
  render: () => (
    <FormWrapper>
      <ControlledBooleanRadioField name="enabled" label="Enabled" trueLabel="Yes" falseLabel="No" readOnly />
    </FormWrapper>
  ),
};
