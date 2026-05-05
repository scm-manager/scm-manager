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

// import { storiesOf } from "@storybook/react";
// import React, { Fragment, useState } from "react";
// import Combobox from "./Combobox";
// import { Combobox as HeadlessCombobox } from "@headlessui/react";
// import { Option } from "@scm-manager/ui-types";
// import { Link, BrowserRouter } from "react-router-dom";
//
// const waitFor = (ms: number) =>
//   function <T>(result: T) {
//     return new Promise<T>((resolve) => setTimeout(() => resolve(result), ms));
//   };
//
// const data = [
//   { label: "Trillian", value: "1" },
//   { label: "Arthur", value: "2" },
//   { label: "Zaphod", value: "3" },
// ];
//
// const linkData = [{ label: "Link111111111111111111111111111111111111", value: "1" }];
//
// storiesOf("Combobox", module)
//   .add("Options array", () => {
//     const [value, setValue] = useState<Option<string>>();
//     return <Combobox options={data} value={value} onChange={setValue} />;
//   })
//   .add("Options function", () => {
//     const [value, setValue] = useState<Option<string>>();
//     return <Combobox options={() => data} value={value} onChange={setValue} />;
//   })
//   .add("Options function as promise", () => {
//     const [value, setValue] = useState<Option<string>>();
//     return (
//       <Combobox
//         value={value}
//         onChange={setValue}
//         options={(query) => Promise.resolve(data.filter((t) => t.label.startsWith(query))).then(waitFor(1000))}
//       />
//     );
//   })
//   .add("Children as Element", () => {
//     const [value, setValue] = useState<Option<string>>();
//     const [query, setQuery] = useState("");
//
//     return (
//       <Combobox value={value} onChange={setValue} onQueryChange={setQuery}>
//         {query ? (
//           <HeadlessCombobox.Option value={{ label: query, value: query }} key={query} as={Fragment}>
//             {({ active }) => <Combobox.Option isActive={active}>{`Create ${query}`}</Combobox.Option>}
//           </HeadlessCombobox.Option>
//         ) : null}
//         <HeadlessCombobox.Option value={{ label: "All", value: "All" }} key="all" as={Fragment}>
//           {({ active }) => <Combobox.Option isActive={active}>All</Combobox.Option>}
//         </HeadlessCombobox.Option>
//         <>
//           {data.map((o) => (
//             <HeadlessCombobox.Option value={o} key={o.value} as={Fragment}>
//               {({ active }) => <Combobox.Option isActive={active}>{o.label}</Combobox.Option>}
//             </HeadlessCombobox.Option>
//           ))}
//         </>
//       </Combobox>
//     );
//   })
//   .add("Children as render props", () => {
//     const [value, setValue] = useState<Option<string>>();
//     return (
//       <Combobox options={data} value={value} onChange={setValue}>
//         {(o) => (
//           <HeadlessCombobox.Option value={o} key={o.value} as={Fragment}>
//             {({ active }) => <Combobox.Option isActive={active}>{o.label}</Combobox.Option>}
//           </HeadlessCombobox.Option>
//         )}
//       </Combobox>
//     );
//   })
//   .add("Links as render props", () => {
//     const [value, setValue] = useState<Option<string>>();
//     const [query, setQuery] = useState("Hello");
//     return (
//       <BrowserRouter>
//         <Combobox
//           className="input is-small omni-search-bar"
//           placeholder="Placeholder"
//           value={value}
//           options={linkData}
//           onChange={setValue}
//           onQueryChange={setQuery}
//         >
//           {(o) => (
//             <HeadlessCombobox.Option value={{ label: o.label, value: query, displayValue: o.value }} key={o.value} as={Fragment}>
//               {({ active }) => (
//                 <Combobox.Option isActive={active}>
//                   <Link to={o.label}>{o.label}</Link>
//                 </Combobox.Option>
//               )}
//             </HeadlessCombobox.Option>
//           )}
//         </Combobox>
//       </BrowserRouter>
//     );
//   });

import React, { Fragment, useState } from "react";
import { BrowserRouter, Link } from "react-router-dom";
import type { Meta, StoryObj } from "@storybook/react";
import { Combobox as HeadlessCombobox } from "@headlessui/react";
import { Option } from "@scm-manager/ui-types";

import Combobox from "./Combobox";

const waitFor =
  (ms: number) =>
  <T,>(result: T) =>
    new Promise<T>((resolve) => setTimeout(() => resolve(result), ms));

const data: Option<string>[] = [
  { label: "Trillian", value: "1" },
  { label: "Arthur", value: "2" },
  { label: "Zaphod", value: "3" },
];

const linkData: Option<string>[] = [{ label: "/user/trillian", value: "1" }];

const meta: Meta<typeof Combobox> = {
  title: "Forms/Combobox",
  // @ts-ignore
  component: Combobox,
  decorators: [(Story) => <div style={{ margin: "2rem", maxWidth: "20rem", minHeight: "15rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

// export const OptionsAsArray: Story = {
//   name: "Options from Array",
//   render: () => {
//     const [value, setValue] = useState<Option<string>>();
//     return <Combobox options={data} value={value} onChange={setValue} />;
//   },
// };
//
// export const OptionsAsFunction: Story = {
//   name: "Options from Function",
//   render: () => {
//     const [value, setValue] = useState<Option<string>>();
//     return <Combobox options={() => data} value={value} onChange={setValue} />;
//   },
// };
//
// export const OptionsAsPromise: Story = {
//   name: "Options from Promise",
//   render: () => {
//     const [value, setValue] = useState<Option<string>>();
//     return (
//       <Combobox
//         value={value}
//         onChange={setValue}
//         options={(query) => Promise.resolve(data.filter((t) => t.label.startsWith(query))).then(waitFor(1000))}
//       />
//     );
//   },
// };

// export const ChildrenAsElement: Story = {
//   name: "Children as Element",
//   render: () => {
//     const [value, setValue] = useState<Option<string>>();
//     const [query, setQuery] = useState("");
//     return (
//       <Combobox value={value} onChange={setValue} onQueryChange={setQuery}>
//         {query ? (
//           <HeadlessCombobox.Option value={{ label: query, value: query }} key={query} as={Fragment}>
//             {({ active }) => <Combobox.Option isActive={active}>{`Create ${query}`}</Combobox.Option>}
//           </HeadlessCombobox.Option>
//         ) : null}
//         <HeadlessCombobox.Option value={{ label: "All", value: "All" }} key="all" as={Fragment}>
//           {({ active }) => <Combobox.Option isActive={active}>All</Combobox.Option>}
//         </HeadlessCombobox.Option>
//         {data.map((o) => (
//           <HeadlessCombobox.Option value={o} key={o.value} as={Fragment}>
//             {({ active }) => <Combobox.Option isActive={active}>{o.label}</Combobox.Option>}
//           </HeadlessCombobox.Option>
//         ))}
//       </Combobox>
//     );
//   },
// };
//
// export const ChildrenAsRenderProps: Story = {
//   name: "Children as Render Props",
//   render: () => {
//     const [value, setValue] = useState<Option<string>>();
//     return (
//       <Combobox options={data} value={value} onChange={setValue}>
//         {(o) => (
//           <HeadlessCombobox.Option value={o} key={o.value} as={Fragment}>
//             {({ active }) => <Combobox.Option isActive={active}>{o.label}</Combobox.Option>}
//           </HeadlessCombobox.Option>
//         )}
//       </Combobox>
//     );
//   },
// };
//
// export const LinksAsRenderProps: Story = {
//   name: "Links as Render Props",
//   render: () => {
//     const [value, setValue] = useState<Option<string>>();
//     return (
//       // Diese Story benötigt ihren eigenen Router-Kontext, um die `Link`-Komponente zu rendern.
//       <BrowserRouter>
//         <Combobox placeholder="Placeholder" value={value} options={linkData} onChange={setValue}>
//           {(o) => (
//             <HeadlessCombobox.Option value={o} key={o.value} as={Fragment}>
//               {({ active }) => (
//                 <Combobox.Option isActive={active}>
//                   <Link to={o.label}>{o.label}</Link>
//                 </Combobox.Option>
//               )}
//             </HeadlessCombobox.Option>
//           )}
//         </Combobox>
//       </BrowserRouter>
//     );
//   },
// };
