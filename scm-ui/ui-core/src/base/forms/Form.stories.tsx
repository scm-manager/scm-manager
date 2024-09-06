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
import React, { useRef, useState } from "react";
import { storiesOf } from "@storybook/react";
import Form from "./Form";
import FormRow from "./FormRow";
import ControlledInputField from "./input/ControlledInputField";
import ControlledSecretConfirmationField from "./input/ControlledSecretConfirmationField";
import ControlledCheckboxField from "./checkbox/ControlledCheckboxField";
import ControlledList from "./list/ControlledList";
import ControlledSelectField from "./select/ControlledSelectField";
import ControlledColumn from "./table/ControlledColumn";
import ControlledTable from "./table/ControlledTable";
import AddListEntryForm from "./AddListEntryForm";
import { ScmFormListContextProvider } from "./ScmFormListContext";
import { HalRepresentation } from "@scm-manager/ui-types";
import ControlledChipInputField from "./chip-input/ControlledChipInputField";
import Combobox from "./combobox/Combobox";
import ControlledComboboxField from "./combobox/ControlledComboboxField";
import { defaultOptionFactory } from "./helpers";
import ChipInput from "./headless-chip-input/ChipInput";

export type SimpleWebHookConfiguration = {
  urlPattern: string;
  executeOnEveryCommit: boolean;
  sendCommitData: boolean;
  method: string;
  headers: WebhookHeader[];
};

export type WebhookHeader = {
  key: string;
  value: string;
  concealed: boolean;
};

storiesOf("Forms", module)
  .add("Creation", () => (
    <Form
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{
        name: "",
        password: "",
        active: true,
      }}
    >
      <FormRow>
        <ControlledInputField rules={{ required: true }} name="name" />
      </FormRow>
      <FormRow>
        <ControlledSecretConfirmationField name="password" />
      </FormRow>
      <FormRow>
        <ControlledCheckboxField name="active" />
      </FormRow>
    </Form>
  ))
  .add("Editing", () => (
    <Form
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{
        name: "trillian",
        password: "secret",
        passwordConfirmation: "",
        active: true,
      }}
    >
      <FormRow>
        <ControlledInputField name="name" />
      </FormRow>
      <FormRow>
        <ControlledSecretConfirmationField name="password" />
      </FormRow>
      <FormRow>
        <ControlledCheckboxField name="active" />
      </FormRow>
    </Form>
  ))
  .add("Reset", () => {
    type FormType = {
      name: string;
      password: string;
      passwordConfirmation: string;
      active: boolean;
      date: string;
    };
    const [defaultValues, setValues] = useState({
      name: "trillian",
      password: "secret",
      passwordConfirmation: "secret",
      active: true,
      date: "",
    });
    const resetValues = useRef({
      name: "",
      password: "",
      passwordConfirmation: "",
      active: false,
      date: "",
    });
    return (
      <Form<FormType>
        onSubmit={(vals) => {
          console.log(vals);
          setValues(vals);
        }}
        translationPath={["sample", "form"]}
        defaultValues={defaultValues as HalRepresentation & FormType}
        withResetTo={resetValues.current}
        withDiscardChanges
      >
        <FormRow>
          <ControlledInputField name="name" />
        </FormRow>
        <FormRow>
          <ControlledSecretConfirmationField name="password" rules={{ required: true }} />
        </FormRow>
        <FormRow>
          <ControlledCheckboxField name="active" />
        </FormRow>
        <FormRow>
          <ControlledInputField name="date" type="date" />
        </FormRow>
      </Form>
    );
  })
  .add("Layout", () => (
    <Form
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{
        name: "",
        password: "",
        method: "",
      }}
    >
      <FormRow>
        <ControlledInputField name="name" />
        <ControlledSelectField name="method">
          {["", "post", "get", "put"].map((value) => (
            <option value={value} key={value}>
              {value}
            </option>
          ))}
        </ControlledSelectField>
        <ControlledSecretConfirmationField name="password" />
      </FormRow>
    </Form>
  ))
  .add("GlobalConfiguration", () => (
    <Form
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{
        url: "",
        filter: "",
        username: "",
        password: "",
        roleLevel: "",
        updateIssues: false,
        disableRepoConfig: false,
      }}
    >
      {({ watch }) => (
        <>
          <FormRow>
            <ControlledInputField name="url" label="URL" helpText="URL of Jira installation (with context path)." />
          </FormRow>
          <FormRow>
            <ControlledInputField name="filter" label="Project Filter" helpText="Filters for jira project key." />
          </FormRow>
          <FormRow>
            <ControlledCheckboxField
              name="updateIssues"
              label="Update Jira Issues"
              helpText="Enable the automatic update function."
            />
          </FormRow>
          <FormRow hidden={watch("filter")}>
            <ControlledInputField
              name="username"
              label="Username"
              helpText="Jira username for connection."
              className="is-half"
            />
            <ControlledInputField
              name="password"
              label="Password"
              helpText="Jira password for connection."
              type="password"
              className="is-half"
            />
          </FormRow>
          <FormRow hidden={watch("filter")}>
            <ControlledInputField
              name="roleLevel"
              label="Role Visibility"
              helpText="Defines for which Project Role the comments are visible."
            />
          </FormRow>
          <FormRow>
            <ControlledCheckboxField
              name="disableRepoConfig"
              label="Do not allow repository configuration"
              helpText="Do not allow repository owners to configure jira instances."
            />
          </FormRow>
        </>
      )}
    </Form>
  ))
  .add("RepoConfiguration", () => (
    <Form
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{
        url: "",
        option: "",
        anotherOption: "",
        disableA: false,
        disableB: false,
        disableC: true,
        labels: ["test"],
        people: [{ displayName: "Trillian", id: 2 }],
        author: null,
      }}
    >
      <ControlledInputField name="url" />
      <ControlledInputField name="option" />
      <ControlledInputField name="anotherOption" />
      <ControlledCheckboxField name="disableA" />
      <ControlledCheckboxField name="disableB" />
      <ControlledCheckboxField name="disableC" />
      <ControlledChipInputField name="labels" />
      <ControlledChipInputField
        name="people"
        optionFactory={(person) => ({ label: person.displayName, value: person })}
      >
        <Combobox
          options={[
            { label: "Zenod", value: { id: 1, displayName: "Zenod" } },
            { label: "Arthur", value: { id: 3, displayName: "Arthur" } },
            { label: "Cookie Monster", value: { id: 4, displayName: "Cookie Monster" } },
          ]}
        />
      </ControlledChipInputField>
      <ControlledComboboxField
        options={["Zenod", "Arthur", "Trillian"].map(defaultOptionFactory)}
        name="author"
        nullable
      />
    </Form>
  ))
  .add("ReadOnly", () => (
    <Form
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{
        name: "trillian",
        password: "secret",
        active: true,
        labels: ["test", "hero"],
      }}
      readOnly
    >
      <FormRow>
        <ControlledInputField name="name" />
      </FormRow>
      <FormRow>
        <ControlledSecretConfirmationField name="password" />
      </FormRow>
      <FormRow>
        <ControlledCheckboxField name="active" />
      </FormRow>
      <FormRow>
        <ControlledChipInputField name="labels" aria-label="Test" placeholder="New label..." />
      </FormRow>
    </Form>
  ))
  .add("Nested", () => (
    <Form
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{
        webhooks: [
          {
            urlPattern: "https://hitchhiker.com",
            executeOnEveryCommit: false,
            sendCommitData: false,
            method: "post",
            headers: [
              {
                key: "test",
                value: "val",
                concealed: false,
              },
              {
                key: "secret",
                value: "password",
                concealed: true,
              },
            ],
          },
          {
            urlPattern: "http://test.com",
            executeOnEveryCommit: false,
            sendCommitData: false,
            method: "get",
            headers: [
              {
                key: "other",
                value: "thing",
                concealed: true,
              },
              {
                key: "stuff",
                value: "haha",
                concealed: false,
              },
            ],
          },
        ],
      }}
      withResetTo={{
        webhooks: [
          {
            urlPattern: "https://other.com",
            executeOnEveryCommit: true,
            sendCommitData: true,
            method: "get",
            headers: [
              {
                key: "change",
                value: "new",
                concealed: true,
              },
            ],
          },
          {
            urlPattern: "http://things.com",
            executeOnEveryCommit: false,
            sendCommitData: true,
            method: "put",
            headers: [
              {
                key: "stuff",
                value: "haha",
                concealed: false,
              },
            ],
          },
        ],
      }}
    >
      <ScmFormListContextProvider name="webhooks">
        <ControlledList withDelete>
          {({ value: webhook }) => (
            <>
              <FormRow>
                <ControlledSelectField name="method">
                  {["post", "get", "put"].map((value) => (
                    <option value={value} key={value}>
                      {value}
                    </option>
                  ))}
                </ControlledSelectField>
                <ControlledInputField name="urlPattern" />
              </FormRow>
              <FormRow>
                <ControlledCheckboxField name="executeOnEveryCommit" />
              </FormRow>
              <FormRow>
                <ControlledCheckboxField name="sendCommitData" />
              </FormRow>
              <details className="has-background-dark-25 mb-2 p-2">
                <summary className="is-clickable">Headers</summary>
                <div>
                  <ScmFormListContextProvider name="headers">
                    <ControlledTable withDelete>
                      <ControlledColumn name="key" />
                      <ControlledColumn name="value" />
                      <ControlledColumn name="concealed">{(value) => (value ? <b>Hallo</b> : null)}</ControlledColumn>
                    </ControlledTable>
                    <AddListEntryForm defaultValues={{ key: "", value: "", concealed: false }}>
                      <FormRow>
                        <ControlledInputField
                          name="key"
                          rules={{
                            validate: (newKey) =>
                              !(webhook as SimpleWebHookConfiguration).headers.some(({ key }) => newKey === key),
                            required: true,
                          }}
                        />
                      </FormRow>
                      <FormRow>
                        <ControlledInputField name="value" />
                      </FormRow>
                      <FormRow>
                        <ControlledCheckboxField name="concealed" />
                      </FormRow>
                    </AddListEntryForm>
                  </ScmFormListContextProvider>
                </div>
              </details>
            </>
          )}
        </ControlledList>
      </ScmFormListContextProvider>
    </Form>
  ))
  .add("Controlled Chip Input with add", () => {
    const ref = useRef<HTMLInputElement>(null);
    return (
      <Form
        onSubmit={console.log}
        translationPath={["sample", "form"]}
        defaultValues={{
          branches: ["main", "develop"],
        }}
      >
        <FormRow>
          <ControlledChipInputField name="branches" ref={ref} />
        </FormRow>
        <FormRow>
          <ChipInput.AddButton inputRef={ref}>Add</ChipInput.AddButton>
        </FormRow>
      </Form>
    );
  });
