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

import React from "react";
import { fireEvent, render, screen, within } from "@testing-library/react";
import Combobox from "./Combobox";

afterEach(() => vi.unstubAllGlobals());

it("renders options in a list and supports keyboard selection", async () => {
  vi.stubGlobal(
    "ResizeObserver",
    class {
      observe() {}
      unobserve() {}
      disconnect() {}
    },
  );
  const onChange = vi.fn();
  const option = { label: "First", value: "first" };
  render(<Combobox aria-label="Choose an option" options={[option]} onChange={onChange} />);

  const input = screen.getByRole("combobox");
  fireEvent.keyDown(input, { key: "ArrowDown" });

  const list = await screen.findByRole("listbox");
  expect(list.tagName).toBe("UL");
  expect(within(list).getByRole("option").tagName).toBe("LI");

  fireEvent.keyDown(input, { key: "Enter" });
  expect(onChange).toHaveBeenCalledWith(option);
});
