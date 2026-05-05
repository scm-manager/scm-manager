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

import React, { FC, useRef, useState } from "react";
import styled from "styled-components";
import { useForm } from "react-hook-form";
import { MemoryRouter } from "react-router-dom";
import type { Meta, StoryObj } from "@storybook/react";

import Textarea from "./Textarea";
import Button from "../buttons/Button";
import { SubmitButton } from "../buttons";

// --- Helper Components & Styles (preserved from your original story) ---

const Spacing = styled.div`
  padding: 2em;
  max-width: 40rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

const OnChangeExample: FC = () => {
  const [value, setValue] = useState("Start typing");
  return (
    <>
      <Textarea value={value} onChange={setValue} />
      <hr />
      <p>{value}</p>
    </>
  );
};

const OnSubmitExample: FC = () => {
  const [value, setValue] = useState("Use ctrl/command + Enter to submit the textarea");
  const [submitted, setSubmitted] = useState("");

  const submit = () => {
    setSubmitted(value);
    setValue("");
  };

  return (
    <>
      <Textarea value={value} onChange={setValue} onSubmit={submit} />
      <hr />
      <p>
        <strong>Submitted:</strong> {submitted}
      </p>
    </>
  );
};

const OnCancelExample: FC = () => {
  const [value, setValue] = useState("Use the escape key to clear the textarea");

  const cancel = () => {
    setValue("");
  };

  return <Textarea value={value} onChange={setValue} onCancel={cancel} />;
};

const RefExample: FC = () => {
  const ref = useRef<HTMLTextAreaElement>(null);
  return (
    <>
      <Textarea ref={ref} placeholder="I will be focused" />
      <Button action={() => ref.current?.focus()} color="primary">
        Focus Textarea
      </Button>
    </>
  );
};

const AutoFocusAndRefExample: FC = () => {
  const ref = useRef<HTMLTextAreaElement>(null);
  return (
    <>
      <Textarea ref={ref} autofocus={true} placeholder="I am focused initially" />
      <Textarea placeholder="Click me to switch focus" />
      <Button action={() => ref.current?.focus()} color="primary">
        Focus First Textarea
      </Button>
    </>
  );
};

type Commit = {
  message: string;
  footer: string;
  readonly: string;
  disabled: string;
};

const ReactHookFormExample: FC = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Commit>();
  const [stored, setStored] = useState<Commit>();

  const onSubmit = (commit: Commit) => {
    setStored(commit);
  };

  return (
    <>
      <form onSubmit={handleSubmit(onSubmit)}>
        <Textarea
          autofocus={true}
          label="Message"
          {...register("message", { required: true })}
          validationError={!!errors.message}
          errorMessage="Message is required"
        />
        <Textarea label="Footer" {...register("footer")} />
        <Textarea
          label="Readonly"
          readOnly={true}
          defaultValue="I am readonly but still show up on submit!"
          {...register("readonly")}
        />
        <Textarea
          label="Disabled"
          disabled={true}
          defaultValue="I am disabled and dont show up on submit!"
          {...register("disabled")}
        />
        <div className="pt-2">
          <SubmitButton>Submit</SubmitButton>
        </div>
      </form>
      {stored && (
        <div className="mt-5">
          <pre>
            <code>{JSON.stringify(stored, null, 2)}</code>
          </pre>
        </div>
      )}
    </>
  );
};

// --- Storybook Metadata ---

const meta: Meta<typeof Textarea> = {
  title: "Forms/Textarea",
  component: Textarea,
  decorators: [(Story) => <Spacing>{Story()}</Spacing>, (Story) => <MemoryRouter>{Story()}</MemoryRouter>],
  argTypes: {
    onChange: { action: "changed" },
    onSubmit: { action: "submitted" },
    onCancel: { action: "cancelled" },
  },
  tags: ["autodocs"],
};

export default meta;

// --- Story Definitions ---

type Story = StoryObj<typeof meta>;

// Simple stories using `args`
export const Default: Story = {
  args: {
    label: "Default Textarea",
    placeholder: "Type something...",
  },
};

export const AutoFocus: Story = {
  args: {
    label: "Field with AutoFocus",
    autofocus: true,
  },
};

export const DefaultValue: Story = {
  args: {
    label: "Field with Default Value",
    defaultValue: "I am a text area with so much default value its crazy!",
  },
};

// Complex stories using `render` and helper components
export const HandlingOnChange: Story = {
  render: () => <OnChangeExample />,
};

export const HandlingOnSubmit: Story = {
  render: () => <OnSubmitExample />,
};

export const HandlingOnCancel: Story = {
  render: () => <OnCancelExample />,
};

export const UsingRefs: Story = {
  render: () => <RefExample />,
};

export const WithAutoFocusAndRef: Story = {
  name: "With AutoFocus and Ref",
  render: () => <AutoFocusAndRefExample />,
};

export const WithReactHookForm: Story = {
  name: "With React Hook Form",
  render: () => <ReactHookFormExample />,
};
