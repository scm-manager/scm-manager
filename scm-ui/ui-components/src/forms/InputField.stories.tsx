/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FC, useRef, useState } from "react";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import InputField from "./InputField";
import Button from "../buttons/Button";
import { MemoryRouter } from "react-router-dom";
import { useForm } from "react-hook-form";
import { SubmitButton } from "../buttons";

const Decorator = styled.div`
  padding: 2rem;
  max-width: 30rem;
`;

const Ref: FC = () => {
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

const AutoFocusAndRef: FC = () => {
  const ref = useRef<HTMLInputElement>(null);
  return (
    <>
      <InputField ref={ref} autofocus={true} placeholder="Click the button to focus me" />
      <InputField placeholder="Click me to switch focus" />
      <Button action={() => ref.current?.focus()} color="primary">
        Focus First InputField
      </Button>
    </>
  );
};

type Name = {
  readonly: string;
  disabled: string;
  firstName: string;
  lastName: string;
};

const ReactHookForm: FC = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Name>();
  const [stored, setStored] = useState<Name>();

  const onSubmit = (person: Name) => {
    setStored(person);
  };

  return (
    <>
      <form onSubmit={handleSubmit(onSubmit)}>
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
          errorMessage={"Last name is required"}
        />
        <div className="pt-2">
          <SubmitButton>Submit</SubmitButton>
        </div>
      </form>
      {stored ? (
        <div className="mt-5">
          <pre>
            <code>{JSON.stringify(stored, null, 2)}</code>
          </pre>
        </div>
      ) : null}
    </>
  );
};

const LegacyEvents: FC = () => {
  const [value, setValue] = useState<string>("");
  return (
    <>
      <InputField placeholder="Legacy onChange handler" value={value} onChange={(e) => setValue(e)} />
      <div className="mt-3">{value}</div>
    </>
  );
};

storiesOf("Forms/InputField", module)
  .addDecorator((storyFn) => <Decorator>{storyFn()}</Decorator>)
  .addDecorator((storyFn) => <MemoryRouter>{storyFn()}</MemoryRouter>)
  .add("AutoFocus", () => <InputField label="Field with AutoFocus" autofocus={true} />)
  .add("Default Value", () => <InputField label="Field with Default Value" defaultValue={"I am a default value"} />)
  .add("Ref", () => <Ref />)
  .add("Legacy Events", () => <LegacyEvents />)
  .add("AutoFocusAndRef", () => <AutoFocusAndRef />)
  .add("React Hook Form", () => <ReactHookForm />);
