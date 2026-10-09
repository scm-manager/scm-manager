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

import React, { useState } from "react";
import { Meta, StoryObj } from "@storybook/react";
import { fn } from "storybook/test";
import Autocomplete, { SuggestionsType } from "./Autocomplete";
import { http, HttpResponse } from "msw";
import { Option, AutocompleteObject } from "@scm-manager/ui-types";

const createSuggestionsHandler = (path: string) =>
  http.get(path, ({ request }) => {
    const query = new URL(request.url).searchParams.get("q") ?? "";
    return HttpResponse.json(
      mockSuggestionsResponse.filter((s) => s.displayName.toLowerCase().includes(query.toLowerCase())),
    );
  });

const mockSuggestionsResponse = [
  { id: "trillian", displayName: "Tricia McMillan" },
  { id: "arthur", displayName: "Arthur Dent" },
  { id: "slartibartfast", displayName: "Slartibartfast" },
];

const meta: Meta<typeof Autocomplete> = {
  title: "Components/Autocomplete",
  component: Autocomplete,
  argTypes: {
    type: { control: { type: "radio" }, options: ["user", "group"] as SuggestionsType[] },
  },
  args: { onSelect: fn() },
};

export default meta;
type Story = StoryObj<typeof Autocomplete>;

export const UserSuggestions: Story = {
  args: { type: "user", autocompleteLink: "autocomplete/users" },
  parameters: {
    msw: {
      handlers: [
        http.get("/api/v2/autocomplete/users", ({ request }) => {
          const query = new URL(request.url).searchParams.get("q") ?? "";
          return HttpResponse.json(
            mockSuggestionsResponse.filter((s) => s.displayName.toLowerCase().includes(query.toLowerCase())),
          );
        }),
      ],
    },
  },
};

export const UserSuggestionsWithClear: Story = {
  args: {
    type: "user",
    autocompleteLink: "autocomplete/users",
    clearAfterSelect: true,
  },
  parameters: { msw: { handlers: [createSuggestionsHandler("/api/v2/autocomplete/users")] } },
  render: (args) => {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    const [selected, setSelected] = useState<Option<AutocompleteObject>[]>([]);

    const handleSelect = (option: Option<AutocompleteObject>) => {
      args.onSelect(option);
      setSelected((prev) => (prev.some((entry) => entry.value.id === option.value.id) ? prev : [...prev, option]));
    };

    return (
      <div>
        <Autocomplete {...args} onSelect={handleSelect} />
        <table style={{ marginTop: "1rem", width: "100%", borderCollapse: "collapse" }}>
          <thead>
            <tr>
              <th style={{ textAlign: "left", borderBottom: "1px solid #ccc", padding: "4px" }}>ID</th>
              <th style={{ textAlign: "left", borderBottom: "1px solid #ccc", padding: "4px" }}>Anzeigename</th>
            </tr>
          </thead>
          <tbody>
            {selected.map((entry) => (
              <tr key={entry.value.id}>
                <td style={{ borderBottom: "1px solid #eee", padding: "4px" }}>{entry.value.id}</td>
                <td style={{ borderBottom: "1px solid #eee", padding: "4px" }}>{entry.value.displayName}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  },
};
