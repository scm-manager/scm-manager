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
import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import DatePicker, { createLocalizedDateFormat, formatDate, parseDate } from "./DatePicker";

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string, variables?: Record<string, string>) =>
      [key, ...Object.values(variables ?? {})].filter(Boolean).join(" "),
    i18n: {
      language: "en",
      languages: ["en"],
    },
  }),
}));

describe("date helpers", () => {
  it("should parse and format ISO dates without changing the day", () => {
    const date = parseDate("2025-05-20");

    expect(date).toBeDefined();
    expect(formatDate(date as Date)).toBe("2025-05-20");
  });

  it("should reject invalid dates", () => {
    expect(parseDate("2025-02-29")).toBeUndefined();
    expect(parseDate("20.05.2025")).toBeUndefined();
  });

  it.each([
    { locale: "de-DE", formatted: "20.05.2025", placeholder: "tt.mm.jjjj" },
    { locale: "es-ES", formatted: "20/05/2025", placeholder: "dd/mm/aaaa" },
    { locale: "en-US", formatted: "05/20/2025", placeholder: "mm/dd/yyyy" },
    { locale: "en-GB", formatted: "20/05/2025", placeholder: "dd/mm/yyyy" },
  ])("should format and parse dates for $locale", ({ locale, formatted, placeholder }) => {
    const localizedDateFormat = createLocalizedDateFormat(locale);
    const date = parseDate("2025-05-20") as Date;

    expect(localizedDateFormat.format(date)).toBe(formatted);
    expect(localizedDateFormat.placeholder).toBe(placeholder);
    expect(formatDate(localizedDateFormat.parse(formatted) as Date)).toBe("2025-05-20");
  });

  it("should accept ISO dates as a fallback for localized input", () => {
    const localizedDateFormat = createLocalizedDateFormat("de-DE");

    expect(formatDate(localizedDateFormat.parse("2025-05-20") as Date)).toBe("2025-05-20");
  });

  it("should reject invalid localized dates", () => {
    const localizedDateFormat = createLocalizedDateFormat("de-DE");

    expect(localizedDateFormat.parse("29.02.2025")).toBeUndefined();
    expect(localizedDateFormat.parse("20/05/2025")).toBeUndefined();
  });

  it("should distinguish complete input from one-digit day or month drafts", () => {
    const localizedDateFormat = createLocalizedDateFormat("en-US");

    expect(localizedDateFormat.isComplete("01/01/2026")).toBe(true);
    expect(localizedDateFormat.isComplete("1/01/2026")).toBe(false);
    expect(localizedDateFormat.isComplete("01/1/2026")).toBe(false);
    expect(localizedDateFormat.isComplete("2026-01-01")).toBe(true);
  });
});

describe("DatePicker", () => {
  const browserDateFormat = createLocalizedDateFormat();

  const ControlledDatePicker = ({
    min,
    max,
    initialValue = "2025-05-20",
  }: {
    min?: string;
    max?: string;
    initialValue?: string;
  }) => {
    const [value, setValue] = useState(initialValue);
    return <DatePicker label="Date" value={value} onChange={setValue} min={min} max={max} />;
  };

  it("should open on click without moving focus or toggling closed", () => {
    render(<ControlledDatePicker />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    input.focus();

    expect(screen.queryByRole("dialog", { name: "Date" })).toBeNull();
    expect(input.getAttribute("aria-haspopup")).toBe("dialog");
    expect(input.getAttribute("aria-expanded")).toBe("false");

    fireEvent.click(input);

    const dialog = screen.getByRole("dialog", { name: "Date" });
    expect(document.activeElement).toBe(input);
    expect(input.getAttribute("aria-expanded")).toBe("true");
    expect(input.getAttribute("aria-controls")).toBe(dialog.id);

    fireEvent.click(input);

    expect(screen.getByRole("dialog", { name: "Date" })).toBeDefined();
    expect(document.activeElement).toBe(input);
  });

  it("should not open when the input only receives focus", () => {
    render(<ControlledDatePicker />);

    screen.getByLabelText<HTMLInputElement>("Date").focus();

    expect(screen.queryByRole("dialog", { name: "Date" })).toBeNull();
  });

  it("should open with ArrowDown, focus the selected day and restore input focus on Escape", async () => {
    render(<ControlledDatePicker />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    input.focus();

    expect(fireEvent.keyDown(input, { key: "ArrowDown" })).toBe(false);

    const selectedDay = screen.getByRole<HTMLButtonElement>("button", { name: /Tuesday, May 20/ });
    expect(document.activeElement).toBe(selectedDay);

    fireEvent.keyDown(selectedDay, { key: "Escape" });

    expect(screen.queryByRole("dialog", { name: "Date" })).toBeNull();
    await waitFor(() => expect(document.activeElement).toBe(input));
  });

  it("should enter an already open calendar with ArrowDown and close it with Tab", () => {
    render(<ControlledDatePicker />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    input.focus();
    fireEvent.click(input);
    fireEvent.keyDown(input, { key: "ArrowDown" });

    expect(document.activeElement).toBe(screen.getByRole("button", { name: /Tuesday, May 20/ }));

    act(() => input.focus());
    expect(fireEvent.keyDown(input, { key: "Tab" })).toBe(true);
    expect(screen.queryByRole("dialog", { name: "Date" })).toBeNull();
  });

  it("should synchronize the visible month only for complete valid dates", () => {
    render(<ControlledDatePicker />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    input.focus();
    fireEvent.click(input);
    fireEvent.change(input, { target: { value: browserDateFormat.format(new Date(2025, 5, 15)) } });

    expect(screen.getByRole("button", { name: /Sunday, June 15/ })).toBeDefined();

    fireEvent.change(input, { target: { value: "invalid" } });
    expect(screen.getByRole("button", { name: /Sunday, June 15/ })).toBeDefined();

    fireEvent.change(input, { target: { value: "" } });
    expect(screen.queryByRole("dialog", { name: "Date" })).toBeNull();

    fireEvent.click(input);
    expect(screen.getByRole("button", { name: /Sunday, June 15/ })).toBeDefined();
  });

  it("should show a click-through calendar glyph only for empty input", () => {
    render(<ControlledDatePicker initialValue="" />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    const calendarIcon = input.parentElement?.querySelector<HTMLElement>(".date-picker-calendar");

    expect(calendarIcon).toBeDefined();
    expect(calendarIcon?.getAttribute("aria-hidden")).toBe("true");
    expect(getComputedStyle(calendarIcon as HTMLElement).pointerEvents).toBe("none");
    expect(input.parentElement?.querySelector(".date-picker-clear")).toBeNull();
    expect(screen.queryByRole("button", { name: /datePicker.openButton.ariaLabel/ })).toBeNull();
  });

  it("should clear through the native search input and close an open popup", () => {
    render(<ControlledDatePicker />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    input.focus();
    fireEvent.click(input);
    expect(screen.getByRole("dialog", { name: "Date" })).toBeDefined();

    fireEvent.change(input, { target: { value: "" } });

    expect(input.value).toBe("");
    expect(screen.queryByRole("dialog", { name: "Date" })).toBeNull();
    expect(document.activeElement).toBe(input);
    expect(input.parentElement?.querySelector(".date-picker-calendar")).toBeDefined();
  });

  it("should provide month and year dropdowns without changing the selected date", () => {
    const onChange = vi.fn();
    render(<DatePicker label="Date" value="2025-05-20" onChange={onChange} />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    fireEvent.click(input);

    const monthSelect = screen.getByRole<HTMLSelectElement>("combobox", { name: /month/i });
    const yearSelect = screen.getByRole<HTMLSelectElement>("combobox", { name: /year/i });
    expect(monthSelect.value).toBe("4");
    expect(yearSelect.value).toBe("2025");
    expect(monthSelect.getAttribute("aria-label")).toBe("datePicker.monthDropdown Choose a month");
    expect(yearSelect.getAttribute("aria-label")).toBe("datePicker.yearDropdown Choose a year");
    expect(screen.getByRole("button", { name: /datePicker.previousMonth/ })).toBeDefined();
    expect(screen.getByRole("button", { name: /datePicker.nextMonth/ })).toBeDefined();

    fireEvent.change(monthSelect, { target: { value: "5" } });
    fireEvent.change(yearSelect, { target: { value: "2024" } });

    expect(screen.getByRole("grid", { name: /June 2024/i })).toBeDefined();
    expect(onChange).not.toHaveBeenCalled();
    expect(input.value).toBe(browserDateFormat.format(new Date(2025, 4, 20)));
  });

  it("should not open a disabled input on click", () => {
    render(<DatePicker label="Date" value="2025-05-20" onChange={vi.fn()} disabled />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");

    fireEvent.click(input);

    expect(screen.queryByRole("dialog", { name: "Date" })).toBeNull();
    expect(input.value).toBe(browserDateFormat.format(new Date(2025, 4, 20)));
    expect(input.disabled).toBe(true);
  });

  it("should display and accept dates using the browser format", () => {
    const onChange = vi.fn();
    render(<DatePicker label="Date" value="2025-05-20" onChange={onChange} />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    expect(input.value).toBe(browserDateFormat.format(new Date(2025, 4, 20)));
    expect(input.placeholder).toBe(browserDateFormat.placeholder);
    expect(input.getAttribute("aria-description")).toBe("datePicker.formatHint Input format {{format}} MM/DD/YYYY");

    fireEvent.change(input, { target: { value: browserDateFormat.format(new Date(2025, 5, 15)) } });
    expect(onChange).toHaveBeenCalledWith("2025-06-15");
  });

  it("should keep a one-digit month as a draft while replacing an existing month", () => {
    const onChange = vi.fn();
    render(<DatePicker label="Date" value="2026-03-01" onChange={onChange} />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    const initialValue = browserDateFormat.format(new Date(2026, 2, 1));
    const monthStart = initialValue.indexOf("03");
    expect(monthStart).toBeGreaterThanOrEqual(0);

    input.setSelectionRange(monthStart, monthStart + 2);
    input.setRangeText("1", monthStart, monthStart + 2, "end");
    fireEvent.input(input);

    const partialValue = `${initialValue.slice(0, monthStart)}1${initialValue.slice(monthStart + 2)}`;
    expect(input.value).toBe(partialValue);
    expect(input.selectionStart).toBe(monthStart + 1);
    expect(onChange).not.toHaveBeenCalled();

    input.setRangeText("2", input.selectionStart as number, input.selectionEnd as number, "end");
    fireEvent.input(input);

    expect(input.value).toBe(browserDateFormat.format(new Date(2026, 11, 1)));
    expect(onChange).toHaveBeenCalledOnce();
    expect(onChange).toHaveBeenCalledWith("2026-12-01");
  });

  it("should normalize a one-digit month when the draft is committed", () => {
    const onChange = vi.fn();
    render(<DatePicker label="Date" value="2026-03-01" onChange={onChange} />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    const initialValue = browserDateFormat.format(new Date(2026, 2, 1));
    const monthStart = initialValue.indexOf("03");
    const partialValue = `${initialValue.slice(0, monthStart)}1${initialValue.slice(monthStart + 2)}`;

    fireEvent.change(input, { target: { value: partialValue } });

    expect(input.value).toBe(partialValue);
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.blur(input);

    expect(input.value).toBe(browserDateFormat.format(new Date(2026, 0, 1)));
    expect(onChange).toHaveBeenCalledOnce();
    expect(onChange).toHaveBeenCalledWith("2026-01-01");
  });

  it("should accept valid ISO dates and clear the value", () => {
    render(<ControlledDatePicker />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    fireEvent.change(input, { target: { value: "2025-06-15" } });
    expect(input.value).toBe(browserDateFormat.format(new Date(2025, 5, 15)));

    expect(input.type).toBe("search");
    expect(input.parentElement?.querySelector(".date-picker-calendar")).toBeNull();

    fireEvent.change(input, { target: { value: "" } });
    expect(input.value).toBe("");
  });

  it("should preserve out-of-bounds input and explain the error on blur", () => {
    render(<ControlledDatePicker min="2025-05-10" max="2025-05-31" />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    const invalidValue = browserDateFormat.format(new Date(2025, 5, 1));
    fireEvent.change(input, { target: { value: invalidValue } });
    fireEvent.blur(input);

    expect(input.value).toBe(invalidValue);
    expect(input.getAttribute("aria-invalid")).toBe("true");
    expect(screen.getByText(/datePicker.validation.max/)).toBeDefined();
  });

  it("should constrain the error and allow clearing an invalid draft without an accepted value", () => {
    render(<ControlledDatePicker initialValue="" max="2025-05-31" />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    fireEvent.change(input, { target: { value: browserDateFormat.format(new Date(2025, 5, 1)) } });
    fireEvent.blur(input);

    const error = screen.getByText(/datePicker.validation.max/);
    expect(getComputedStyle(error).position).toBe("static");
    expect(getComputedStyle(error).maxWidth).toBe("100%");
    expect(getComputedStyle(error).overflowWrap).toBe("anywhere");

    fireEvent.change(input, { target: { value: "" } });

    expect(input.value).toBe("");
    expect(screen.queryByText(/datePicker.validation.max/)).toBeNull();
  });

  it("should defer malformed-date errors until Enter and clear them after correction", () => {
    render(<ControlledDatePicker />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    fireEvent.change(input, { target: { value: "not-a-date" } });
    expect(screen.queryByText(/datePicker.validation.invalid/)).toBeNull();

    fireEvent.keyDown(input, { key: "Enter" });
    expect(input.value).toBe("not-a-date");
    expect(screen.getByText(/datePicker.validation.invalid/)).toBeDefined();

    fireEvent.change(input, { target: { value: browserDateFormat.format(new Date(2025, 5, 15)) } });
    expect(input.getAttribute("aria-invalid")).toBeNull();
    expect(screen.queryByText(/datePicker.validation.invalid/)).toBeNull();

    fireEvent.change(input, { target: { value: "still-not-a-date" } });
    fireEvent.keyDown(input, { key: "Enter" });
    fireEvent.change(input, { target: { value: "" } });

    expect(input.value).toBe("");
    expect(screen.queryByText(/datePicker.validation.invalid/)).toBeNull();
  });

  it("should apply bounds to the calendar", () => {
    render(<ControlledDatePicker min="2025-05-15" max="2025-05-25" />);

    const input = screen.getByLabelText<HTMLInputElement>("Date");
    fireEvent.change(input, { target: { value: browserDateFormat.format(new Date(2025, 4, 14)) } });
    fireEvent.blur(input);
    expect(input.getAttribute("aria-invalid")).toBe("true");

    fireEvent.click(input);

    expect(screen.getByRole<HTMLButtonElement>("button", { name: /Wednesday, May 14/ }).disabled).toBe(true);
    const selectableDay = screen.getByRole<HTMLButtonElement>("button", { name: /Thursday, May 15/ });
    expect(selectableDay.disabled).toBe(false);
    expect(screen.getByRole<HTMLButtonElement>("button", { name: /Monday, May 26/ }).disabled).toBe(true);

    fireEvent.click(selectableDay);

    expect(screen.getByLabelText<HTMLInputElement>("Date").value).toBe(browserDateFormat.format(new Date(2025, 4, 15)));
    expect(input.getAttribute("aria-invalid")).toBeNull();
  });

  it("should always render six weeks to keep the popover height stable", () => {
    render(<ControlledDatePicker />);

    fireEvent.click(screen.getByLabelText<HTMLInputElement>("Date"));

    expect(screen.getByRole("dialog", { name: "Date" }).querySelectorAll(".rdp-week")).toHaveLength(6);
  });
});
