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
