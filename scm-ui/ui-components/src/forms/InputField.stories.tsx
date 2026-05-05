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

// import React, { FC, useRef, useState } from "react";
// import { storiesOf } from "@storybook/react";
// import styled from "styled-components";
// import InputField from "./InputField";
// import Button from "../buttons/Button";
// import { MemoryRouter } from "react-router-dom";
// import { useForm } from "react-hook-form";
// import { SubmitButton } from "../buttons";
//
// const Decorator = styled.div`
//   padding: 2rem;
//   max-width: 30rem;
// `;
//
// const Ref: FC = () => {
//   const ref = useRef<HTMLInputElement>(null);
//   return (
//     <>
//       <InputField ref={ref} placeholder="Click the button to focus me" />
//       <Button action={() => ref.current?.focus()} color="primary">
//         Focus InputField
//       </Button>
//     </>
//   );
// };
//
// const AutoFocusAndRef: FC = () => {
//   const ref = useRef<HTMLInputElement>(null);
//   return (
//     <>
//       <InputField ref={ref} autofocus={true} placeholder="Click the button to focus me" />
//       <InputField placeholder="Click me to switch focus" />
//       <Button action={() => ref.current?.focus()} color="primary">
//         Focus First InputField
//       </Button>
//     </>
//   );
// };
//
// type Name = {
//   readonly: string;
//   disabled: string;
//   firstName: string;
//   lastName: string;
// };
//
// const ReactHookForm: FC = () => {
//   const {
//     register,
//     handleSubmit,
//     formState: { errors },
//   } = useForm<Name>();
//   const [stored, setStored] = useState<Name>();
//
//   const onSubmit = (person: Name) => {
//     setStored(person);
//   };
//
//   return (
//     <>
//       <form onSubmit={handleSubmit(onSubmit)}>
//         <InputField
//           label="Readonly"
//           defaultValue="I am readonly but still show up on submit!"
//           readOnly={true}
//           {...register("readonly")}
//         />
//         <InputField
//           label="Disabled"
//           defaultValue="I am disabled and dont show up on submit!"
//           disabled={true}
//           {...register("disabled")}
//         />
//         <InputField label="First Name" autofocus={true} {...register("firstName")} />
//         <InputField
//           label="Last Name"
//           {...register("lastName", { required: true })}
//           validationError={!!errors.lastName}
//           errorMessage="Last name is required"
//         />
//         <div className="pt-2">
//           <SubmitButton>Submit</SubmitButton>
//         </div>
//       </form>
//       {stored ? (
//         <div className="mt-5">
//           <pre>
//             <code>{JSON.stringify(stored, null, 2)}</code>
//           </pre>
//         </div>
//       ) : null}
//     </>
//   );
// };
//
// const LegacyEvents: FC = () => {
//   const [value, setValue] = useState<string>("");
//   return (
//     <>
//       <InputField placeholder="Legacy onChange handler" value={value} onChange={(e) => setValue(e)} />
//       <div className="mt-3">{value}</div>
//     </>
//   );
// };
//
// storiesOf("Forms/InputField", module)
//   .addDecorator((storyFn) => <Decorator>{storyFn()}</Decorator>)
//   .addDecorator((storyFn) => <MemoryRouter>{storyFn()}</MemoryRouter>)
//   .add("AutoFocus", () => <InputField label="Field with AutoFocus" autofocus={true} />)
//   .add("Default Value", () => <InputField label="Field with Default Value" defaultValue="I am a default value" />)
//   .add("Ref", () => <Ref />)
//   .add("Legacy Events", () => <LegacyEvents />)
//   .add("AutoFocusAndRef", () => <AutoFocusAndRef />)
//   .add("React Hook Form", () => <ReactHookForm />);

import React, { FC, useRef, useState } from "react";
import type { Meta, StoryObj } from "@storybook/react";
import styled from "styled-components";
import { useForm } from "react-hook-form";
import { MemoryRouter } from "react-router-dom";

import InputField from "./InputField";
import Button from "../buttons/Button";
import { SubmitButton } from "../buttons";

const Decorator = styled.div`
  padding: 2rem;
  max-width: 30rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

const RefExample: FC = () => {
  const ref = useRef<HTMLInputElement>(null);
  return (
    <>
      <InputField ref={ref} placeholder="Click the button to focus me" />
      <Button action={() => ref.current?.focus()} color="primary">
        Focus InputField
      </Button>
    </>
  );
};

const OnChangeExample: FC = () => {
  const [value, setValue] = useState("");
  return (
    <>
      {/* @ts-ignore*/}
      <InputField
        placeholder="Legacy onChange handler"
        value={value}
        onChange={(e: React.ChangeEvent<HTMLInputElement>) => setValue(e.target.value)}
      />
      <div className="mt-3">
        <strong>Value:</strong> {value}
      </div>
    </>
  );
};

// Helper-Komponente für die "ReactHookForm"-Story
type FormValues = {
  readonly: string;
  disabled: string;
  firstName: string;
  lastName: string;
};

const ReactHookFormExample: FC = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>();
  const [stored, setStored] = useState<FormValues>();

  const onSubmit = (data: FormValues) => {
    setStored(data);
  };

  return (
    <>
      <form onSubmit={handleSubmit(onSubmit)} style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
        <InputField
          label="Readonly"
          defaultValue="I am readonly but still show up on submit!"
          readOnly={true}
          {...register("readonly")}
        />
        <InputField
          label="Disabled"
          defaultValue="I am disabled and dont show up on submit!"
          disabled={true}
          {...register("disabled")}
        />
        <InputField label="First Name" autofocus={true} {...register("firstName")} />
        <InputField
          label="Last Name"
          {...register("lastName", { required: true })}
          validationError={!!errors.lastName}
          errorMessage="Last name is required"
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

const meta: Meta<typeof InputField> = {
  title: "Forms/InputField",
  component: InputField,
  decorators: [
    (Story) => (
      <Decorator>
        <Story />
      </Decorator>
    ),
    (Story) => (
      <MemoryRouter>
        <Story />
      </MemoryRouter>
    ),
  ],
  argTypes: {
    onChange: { action: "changed" },
  },
  tags: ["autodocs"],
};

export default meta;

// --- Story Definitions ---

type Story = StoryObj<typeof meta>;

// Einfache Stories, die direkt `args` verwenden
export const Default: Story = {
  args: {
    label: "Default Input",
    placeholder: "Type something here...",
  },
};

export const WithHelpText: Story = {
  args: {
    ...Default.args,
    label: "With Help Text",
    helpText: "This is some helpful information.",
  },
};

export const Disabled: Story = {
  args: {
    ...Default.args,
    label: "Disabled Input",
    disabled: true,
  },
};

// Komplexe Stories, die eine `render`-Funktion benötigen
export const WithRef: Story = {
  name: "Using Refs",
  render: () => <RefExample />,
};

export const WithOnChange: Story = {
  name: "Handling onChange",
  render: () => <OnChangeExample />,
};

export const WithReactHookForm: Story = {
  name: "With React Hook Form",
  render: () => <ReactHookFormExample />,
};
