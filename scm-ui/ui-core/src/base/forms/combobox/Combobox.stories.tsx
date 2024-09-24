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

import { storiesOf } from "@storybook/react";
import React, { Fragment, useState } from "react";
import Combobox from "./Combobox";
import { Combobox as HeadlessCombobox } from "@headlessui/react";
import { Option } from "@scm-manager/ui-types";
import { Link, BrowserRouter } from "react-router-dom";

const waitFor = (ms: number) =>
  function <T>(result: T) {
    return new Promise<T>((resolve) => setTimeout(() => resolve(result), ms));
  };

const data = [
  { label: "Trillian", value: "1" },
  { label: "Arthur", value: "2" },
  { label: "Zaphod", value: "3" },
];

const linkData = [{ label: "Link111111111111111111111111111111111111", value: "1" }];

storiesOf("Combobox", module)
  .add("Options array", () => {
    const [value, setValue] = useState<Option<string>>();
    return <Combobox options={data} value={value} onChange={setValue} />;
  })
  .add("Options function", () => {
    const [value, setValue] = useState<Option<string>>();
    return <Combobox options={() => data} value={value} onChange={setValue} />;
  })
  .add("Options function as promise", () => {
    const [value, setValue] = useState<Option<string>>();
    return (
      <Combobox
        value={value}
        onChange={setValue}
        options={(query) => Promise.resolve(data.filter((t) => t.label.startsWith(query))).then(waitFor(1000))}
      />
    );
  })
  .add("Children as Element", () => {
    const [value, setValue] = useState<Option<string>>();
    const [query, setQuery] = useState("");

    return (
      <Combobox value={value} onChange={setValue} onQueryChange={setQuery}>
        {query ? (
          <HeadlessCombobox.Option value={{ label: query, value: query }} key={query} as={Fragment}>
            {({ active }) => <Combobox.Option isActive={active}>{`Create ${query}`}</Combobox.Option>}
          </HeadlessCombobox.Option>
        ) : null}
        <HeadlessCombobox.Option value={{ label: "All", value: "All" }} key="all" as={Fragment}>
          {({ active }) => <Combobox.Option isActive={active}>All</Combobox.Option>}
        </HeadlessCombobox.Option>
        <>
          {data.map((o) => (
            <HeadlessCombobox.Option value={o} key={o.value} as={Fragment}>
              {({ active }) => <Combobox.Option isActive={active}>{o.label}</Combobox.Option>}
            </HeadlessCombobox.Option>
          ))}
        </>
      </Combobox>
    );
  })
  .add("Children as render props", () => {
    const [value, setValue] = useState<Option<string>>();
    return (
      <Combobox options={data} value={value} onChange={setValue}>
        {(o) => (
          <HeadlessCombobox.Option value={o} key={o.value} as={Fragment}>
            {({ active }) => <Combobox.Option isActive={active}>{o.label}</Combobox.Option>}
          </HeadlessCombobox.Option>
        )}
      </Combobox>
    );
  })
  .add("Links as render props", () => {
    const [value, setValue] = useState<Option<string>>();
    const [query, setQuery] = useState("Hello");
    return (
      <BrowserRouter>
        <Combobox
          className="input is-small omni-search-bar"
          placeholder={"Placeholder"}
          value={value}
          options={linkData}
          onChange={setValue}
          onQueryChange={setQuery}
        >
          {(o) => (
            <HeadlessCombobox.Option value={{ label: o.label, value: query, displayValue: o.value }} key={o.value} as={Fragment}>
              {({ active }) => (
                <Combobox.Option isActive={active}>
                  <Link to={o.label}>{o.label}</Link>
                </Combobox.Option>
              )}
            </HeadlessCombobox.Option>
          )}
        </Combobox>
      </BrowserRouter>
    );
  });
