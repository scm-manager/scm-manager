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
import Button from "../buttons/Button";
import { useForm } from "react-hook-form";
import { SubmitButton } from "../buttons";
import { storiesOf } from "@storybook/react";
import { MemoryRouter } from "react-router-dom";
import Select from "./Select";

const Ref: FC = () => {
  const ref = useRef<HTMLSelectElement>(null);
  const [selected, setSelected] = useState("false");
  return (
    <>
      <Select
        options={[
          { label: "foo", value: "true" },
          { label: "bar", value: "false" },
        ]}
        value={selected}
        label={"Ref Radio Button"}
        onChange={(e) => setSelected(e.target.value)}
        ref={ref}
      />
      <Button
        action={() => {
          if (ref.current) {
            ref.current.focus();
          }
        }}
        color="primary"
      >
        Focus Select Field
      </Button>
      {selected}
    </>
  );
};

type Settings = {
  rememberMe: string;
  scramblePassword: string;
  disabled: string;
  readonly: string;
};

const ReactHookForm: FC = () => {
  const { register, handleSubmit } = useForm<Settings>();
  const [stored, setStored] = useState<Settings>();

  const onSubmit = (settings: Settings) => {
    setStored(settings);
  };

  return (
    <>
      <form onSubmit={handleSubmit(onSubmit)}>
        <Select
          options={[
            { label: "Yes", value: "true" },
            { label: "No", value: "false" },
          ]}
          label="Remember Me"
          {...register("rememberMe")}
        />
        <Select
          options={[
            { label: "Yes", value: "true" },
            { label: "No", value: "false" },
          ]}
          label="Scramble Password"
          {...register("scramblePassword")}
        />
        <Select
          options={[
            { label: "Yes", value: "true" },
            { label: "No", value: "false" },
          ]}
          label="Disabled wont be submitted"
          defaultValue="false"
          disabled={true}
          {...register("disabled")}
        />
        <Select
          options={[
            { label: "Yes", value: "true" },
            { label: "No", value: "false" },
          ]}
          label="Readonly will be submitted"
          readOnly={true}
          defaultValue="false"
          {...register("readonly")}
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
  const [value, setValue] = useState<string>();
  return (
    <>
      <Select
        options={[
          { label: "Yes", value: "true" },
          { label: "No", value: "false" },
        ]}
        onChange={setValue}
      />
      <div className="mt-3">{JSON.stringify(value)}</div>
    </>
  );
};

const AddNoExistingValue: FC = () => {
  const [value, setValue] = useState<string>("uscss-prometheus");

  return (
    <>
      <Select
        options={[
          {
            label: "Millennium Falcon",
            value: "millennium-falcon",
          },
          {
            label: "Razor Crest",
            value: "razor-crest",
          },
        ]}
        onChange={setValue}
        addValueToOptions={true}
        value={value}
      />
      <div className="mt-3">{value}</div>
    </>
  );
};

const PreselectOption: FC = () => {
  const [value, setValue] = useState<string>("razor-crest");

  return (
    <>
      <Select
        options={[
          {
            label: "Millennium Falcon",
            value: "millennium-falcon",
          },
          {
            label: "Razor Crest",
            value: "razor-crest",
          },
          {
            label: "USCSS Prometheus",
            value: "uscss-prometheus"
          }
        ]}
        onChange={setValue}
        value={value}
      />
      <div className="mt-3">{value}</div>
    </>
  );
};

storiesOf("Forms/Select", module)
  .addDecorator((storyFn) => <MemoryRouter>{storyFn()}</MemoryRouter>)
  .add("Add no existing value", () => <AddNoExistingValue />)
  .add("Ref", () => <Ref />)
  .add("Legacy Events", () => <LegacyEvents />)
  .add("ReactHookForm", () => <ReactHookForm />)
  .add("Preselect option", () => <PreselectOption />)
;
