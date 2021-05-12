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
          { label: "bar", value: "false" }
        ]}
        value={selected}
        label={"Ref Radio Button"}
        onChange={e => setSelected(e.target.value)}
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
            { label: "No", value: "false" }
          ]}
          label="Remember Me"
          {...register("rememberMe")}
        />
        <Select
          options={[
            { label: "Yes", value: "true" },
            { label: "No", value: "false" }
          ]}
          label="Scramble Password"
          {...register("scramblePassword")}
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
          { label: "No", value: "false" }
        ]}
        onChange={setValue}
      />
      <div className="mt-3">{JSON.stringify(value)}</div>
    </>
  );
};

storiesOf("Forms|Select", module)
  .addDecorator(storyFn => <MemoryRouter>{storyFn()}</MemoryRouter>)
  .add("Ref", () => <Ref />)
  .add("Legacy Events", () => <LegacyEvents />)
  .add("ReactHookForm", () => <ReactHookForm />);
