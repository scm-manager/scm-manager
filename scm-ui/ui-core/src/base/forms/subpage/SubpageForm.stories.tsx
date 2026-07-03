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

import React, { FC, useState } from "react";
import type { Meta, StoryObj } from "@storybook/react";

import SubpageForm from "./SubpageForm";
import SubpageContainer from "./SubpageContainer";
import Subpage, { Divider, OpenIconButton, SubpageHeader } from "./Subpage";
import SubpageTable from "./SubpageTable";
import { useScmFormContext } from "../ScmFormContext";
import FormRow from "../FormRow";
import ControlledChipInputField from "../chip-input/ControlledChipInputField";
import ControlledSelectField from "../select/ControlledSelectField";
import ControlledInputField from "../input/ControlledInputField";
import { Icon } from "../../buttons";

const meta: Meta<typeof SubpageForm> = {
  title: "Forms/SubpageForm",
  component: SubpageForm,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "60rem" }}>{Story()}</div>],
  tags: ["autodocs"],
  parameters: {
    docs: {
      description: {
        component:
          "`SubpageForm` is a read-only overview that navigates into editable `Subpage`s via the URL " +
          "(`?subpage=<name>`). These stories focus on the ways to get from the overview into a subpage and " +
          "back: the `SubpageHeader` edit link, `OpenIconButton` row actions inside a table, the breadcrumb, " +
          "and the read-only case where navigation is disabled. The table styling and the boolean radio field " +
          "each have their own story (`SubpageTable`, `ControlledBooleanRadioField`).",
      },
    },
  },
};

export default meta;

type Story = StoryObj<typeof meta>;

/** Live view of the current form values, so a subpage save can be seen reflected back in the overview. */
const OverviewValues: FC = () => {
  const { watch } = useScmFormContext();
  return (
    <pre className="has-background-secondary-least p-4 mb-4">
      <code>{JSON.stringify(watch(), null, 2)}</code>
    </pre>
  );
};

type ConfigType = {
  addresses: string[];
  sendAs: string;
  maxDiffLines: number;
};

/**
 * Navigation via `SubpageHeader`: each section header carries an edit link that opens its subpage. Saving a
 * subpage submits the whole configuration and returns to the overview; the breadcrumb also navigates back.
 */
const HeaderNavigationExample: FC<{ readOnly?: boolean }> = ({ readOnly }) => {
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
      successMessageFallback="Configuration changed successfully!"
      onSubmit={(values) => setData(values)}
    >
      <SubpageContainer>
        <SubpageHeader subpage="emails" label="Send notifications to">
          Edit list
        </SubpageHeader>
        <OverviewValues />
        <Divider />
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

export const HeaderNavigation: Story = {
  render: () => <HeaderNavigationExample />,
};

export const ReadOnly: Story = {
  render: () => <HeaderNavigationExample readOnly />,
  parameters: {
    docs: {
      description: {
        story: "In read-only mode the edit links are hidden, so no subpage can be opened.",
      },
    },
  },
};

type RecipientConfig = {
  recipients: string[];
  sendAs: string;
};

/** Navigation via `OpenIconButton` row actions inside a `SubpageTable`: each row opens the same subpage. */
const TableNavigationExample: FC = () => {
  const [data, setData] = useState<RecipientConfig>({
    recipients: ["beate-paulina.berlin@standardemail.de", "paul.bernd@email.de"],
    sendAs: "committer",
  });
  return (
    <SubpageForm<RecipientConfig>
      title="E-Mail notifications"
      translationPath={["sample", "form"]}
      defaultValues={data}
      successMessageFallback="Configuration changed successfully!"
      onSubmit={(values) => setData(values)}
    >
      <SubpageContainer>
        <SubpageHeader subpage="settings" label="Delivery settings">
          Edit settings
        </SubpageHeader>
        <OverviewValues />
        <Divider />
        {/* Label-only header (no children => no edit button); the row actions below handle navigation. */}
        <SubpageHeader label="Recipients" />
        <SubpageTable>
          <thead>
            <tr>
              <th>Email address</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {data.recipients.map((email) => (
              <tr key={email}>
                <td data-label="Email address">{email}</td>
                <td data-label="Actions">
                  <OpenIconButton subpage="recipients" aria-label={`Edit recipient ${email}`}>
                    <Icon>edit</Icon>
                  </OpenIconButton>
                </td>
              </tr>
            ))}
          </tbody>
        </SubpageTable>
      </SubpageContainer>
      <Subpage name="recipients" title="Edit recipients">
        <FormRow>
          {/* @ts-ignore ControlledChipInputField supports string lists */}
          <ControlledChipInputField name="recipients" label="Email address" placeholder="Add address..." />
        </FormRow>
      </Subpage>
      <Subpage name="settings" title="Delivery settings">
        <FormRow>
          <ControlledSelectField name="sendAs" label="Send as">
            <option value="committer">Committer</option>
            <option value="author">Author</option>
          </ControlledSelectField>
        </FormRow>
      </Subpage>
    </SubpageForm>
  );
};

export const OpenButtonsInTable: Story = {
  render: () => <TableNavigationExample />,
  parameters: {
    docs: {
      description: {
        story:
          "The *Recipients* header is a label-only `SubpageHeader` (no children, so no edit button); navigation " +
          "is handled per row by the `OpenIconButton`, which opens the *recipients* subpage. The table's own " +
          "responsive/stacking behaviour is covered in the dedicated `SubpageTable` story.",
      },
    },
  },
};
