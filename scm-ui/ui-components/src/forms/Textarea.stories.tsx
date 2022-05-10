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
import Textarea from "./Textarea";
import Button from "../buttons/Button";
import { useForm } from "react-hook-form";
import { SubmitButton } from "../buttons";
import { MemoryRouter } from "react-router-dom";

const Spacing = styled.div`
  padding: 2em;
`;

const OnChangeTextarea = () => {
  const [value, setValue] = useState("Start typing");
  return (
    <Spacing>
      <Textarea value={value} onChange={(v) => setValue(v)} />
      <hr />
      <p>{value}</p>
    </Spacing>
  );
};

const OnSubmitTextare = () => {
  const [value, setValue] = useState("Use the ctrl/command + Enter to submit the textarea");
  const [submitted, setSubmitted] = useState("");

  const submit = () => {
    setSubmitted(value);
    setValue("");
  };

  return (
    <Spacing>
      <Textarea value={value} onChange={(v) => setValue(v)} onSubmit={submit} />
      <hr />
      <p>{submitted}</p>
    </Spacing>
  );
};

const OnCancelTextarea = () => {
  const [value, setValue] = useState("Use the escape key to clear the textarea");

  const cancel = () => {
    setValue("");
  };

  return (
    <Spacing>
      <Textarea value={value} onChange={(v) => setValue(v)} onCancel={cancel} />
    </Spacing>
  );
};

const Ref: FC = () => {
  const ref = useRef<HTMLTextAreaElement>(null);
  return (
    <>
      <Textarea ref={ref} />
      <Button action={() => ref.current?.focus()} color="primary">
        Focus InputField
      </Button>
    </>
  );
};

const AutoFocusAndRef: FC = () => {
  const ref = useRef<HTMLTextAreaElement>(null);
  return (
    <>
      <Textarea ref={ref} autofocus={true} placeholder="Click the button to focus me" />
      <Textarea placeholder="Click me to switch focus" />
      <Button action={() => ref.current?.focus()} color="primary">
        Focus First InputField
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

const ReactHookForm: FC = () => {
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
          errorMessage={"Message is required"}
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
      <Textarea placeholder="Legacy onChange handler" value={value} onChange={(e) => setValue(e)} />
      <div className="mt-3">{value}</div>
    </>
  );
};

storiesOf("Forms/Textarea", module)
  .addDecorator((storyFn) => <MemoryRouter>{storyFn()}</MemoryRouter>)
  .add("OnChange", () => <OnChangeTextarea />)
  .add("OnSubmit", () => <OnSubmitTextare />)
  .add("OnCancel", () => <OnCancelTextarea />)
  .add("AutoFocus", () => <Textarea label="Field with AutoFocus" autofocus={true} />)
  .add("Default Value", () => (
    <Textarea
      label="Field with Default Value"
      defaultValue={"I am a text area with so much default value its crazy!"}
    />
  ))
  .add("Ref", () => <Ref />)
  .add("Legacy Events", () => <LegacyEvents />)
  .add("AutoFocusAndRef", () => <AutoFocusAndRef />)
  .add("ReactHookForm", () => <ReactHookForm />);
