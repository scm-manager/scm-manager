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

/* eslint-disable no-console */
import React, { FC, useState } from "react";
import type { Meta, StoryObj } from "@storybook/react";
import SubpageForm from "./SubpageForm";
import SubpageContainer from "./SubpageContainer";
import Subpage, { SubpageHeader } from "./Subpage";
import { useScmFormContext } from "../ScmFormContext";
import FormRow from "../FormRow";
import ControlledChipInputField from "../chip-input/ControlledChipInputField";
import ControlledSelectField from "../select/ControlledSelectField";
import ControlledInputField from "../input/ControlledInputField";

const meta: Meta<typeof SubpageForm> = {
  title: "Forms/SubpageForm",
  component: SubpageForm,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "60rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

const OverviewValues: FC = () => {
  const { watch } = useScmFormContext();
  const values = watch();
  return (
    <pre className="has-background-secondary-least p-4 mb-4">
      <code>{JSON.stringify(values, null, 2)}</code>
    </pre>
  );
};

type ConfigType = {
  addresses: string[];
  sendAs: string;
  maxDiffLines: number;
};

const SubpageFormExample: FC<{ readOnly?: boolean }> = ({ readOnly }) => {
  const [data, setData] = useState<ConfigType>({
    addresses: ["beate-paulina.berlin@standardemail.de", "paul.bernd@email.de"],
    sendAs: "committer",
    maxDiffLines: 444,
  });
  return (
    <SubpageForm<ConfigType>
      title="E-Mail notifications for commits"
      translationPath={["sample", "form"]}
      defaultValues={data}
      readOnly={readOnly}
      onSubmit={(values) => {
        console.log(values);
        setData(values);
      }}
    >
      <SubpageContainer>
        <SubpageHeader subpage="emails" label="Send notifications to">
          Edit list
        </SubpageHeader>
        <OverviewValues />
        <SubpageHeader subpage="settings" label="Commit notification settings">
          Edit settings
        </SubpageHeader>
        <OverviewValues />
      </SubpageContainer>
      <Subpage name="emails" title="Edit email list">
        <FormRow>
          {/* @ts-ignore ControlledChipInputField supports string lists */}
          <ControlledChipInputField name="addresses" label="Email address" placeholder="Add address..." />
        </FormRow>
      </Subpage>
      <Subpage name="settings" title="Edit settings">
        <FormRow>
          <ControlledSelectField name="sendAs" label="Send as">
            <option value="committer">Committer</option>
            <option value="author">Author</option>
          </ControlledSelectField>
        </FormRow>
        <FormRow>
          <ControlledInputField name="maxDiffLines" label="Max diff lines" type="number" />
        </FormRow>
      </Subpage>
    </SubpageForm>
  );
};

export const Default: Story = { render: () => <SubpageFormExample /> };

export const ReadOnly: Story = { render: () => <SubpageFormExample readOnly /> };
