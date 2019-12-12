import React, {useState} from "react";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import Textarea from "./Textarea";

const Spacing = styled.div`
  padding: 2em;
`;

const OnChangeTextarea = () => {
  const [value, setValue] = useState("Start typing");
  return (
    <Spacing>
      <Textarea value={value} onChange={v => setValue(v)} />
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
      <Textarea value={value} onChange={v => setValue(v)} onSubmit={submit} />
      <hr />
      <p>{submitted}</p>
    </Spacing>
  );
};

const OnCancelTextare = () => {
  const [value, setValue] = useState("Use the escape key to clear the textarea");

  const cancel = () => {
    setValue("");
  };

  return (
    <Spacing>
      <Textarea value={value} onChange={v => setValue(v)} onCancel={cancel} />
    </Spacing>
  );
};

storiesOf("Forms|Textarea", module)
  .add("OnChange", () => <OnChangeTextarea />)
  .add("OnSubmit", () => <OnSubmitTextare />)
  .add("OnCancel", () => <OnCancelTextare />);
