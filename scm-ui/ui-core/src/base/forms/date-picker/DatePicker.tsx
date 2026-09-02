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

import React, { ChangeEvent, FC, KeyboardEvent, useEffect, useMemo, useRef, useState } from "react";
import * as RadixPopover from "@radix-ui/react-popover";
import { DayPicker } from "@daypicker/react";
import "@daypicker/react/style.css";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import Field from "../base/Field";
import Control from "../base/Control";
import FieldMessage from "../base/field-message/FieldMessage";
import Label from "../base/label/Label";
import Input from "../input/Input";
import { Icon } from "../../buttons";
import { useAriaId } from "../../helpers";
import { chooseLocale } from "../../misc";

const ISO_DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

export const parseDate = (value?: string): Date | undefined => {
  if (!value || !ISO_DATE_PATTERN.test(value)) {
    return undefined;
  }

  const [year, month, day] = value.split("-").map(Number);
  const date = new Date(year, month - 1, day);
  if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
    return undefined;
  }
  return date;
};

export const formatDate = (date: Date): string => {
  const year = date.getFullYear().toString().padStart(4, "0");
  const month = (date.getMonth() + 1).toString().padStart(2, "0");
  const day = date.getDate().toString().padStart(2, "0");
  return `${year}-${month}-${day}`;
};

type DatePart = "day" | "month" | "year";

const DATE_PARTS: DatePart[] = ["day", "month", "year"];
const DEFAULT_PLACEHOLDER_PARTS: Record<DatePart, string> = { day: "dd", month: "mm", year: "yyyy" };
const PLACEHOLDER_PARTS_BY_LANGUAGE: Record<string, Record<DatePart, string>> = {
  de: { day: "tt", month: "mm", year: "jjjj" },
  es: { day: "dd", month: "mm", year: "aaaa" },
};
const BIDI_MARK_PATTERN = /[\u061c\u200e\u200f]/g;
const LOCALIZED_DATE_OPTIONS: Intl.DateTimeFormatOptions = {
  calendar: "gregory",
  numberingSystem: "latn",
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
};

const isDatePart = (value: Intl.DateTimeFormatPartTypes): value is DatePart => DATE_PARTS.includes(value as DatePart);

const escapeRegExp = (value: string) =>
  value
    .replace(BIDI_MARK_PATTERN, "")
    .replace(/[.*+?^${}()|[\]\\]/g, "\\$&")
    .replace(/\s+/g, "\\s*");

export const createLocalizedDateFormat = (locale?: string) => {
  const formatter = new Intl.DateTimeFormat(locale, LOCALIZED_DATE_OPTIONS);
  const parts = formatter.formatToParts(new Date(2006, 10, 22));
  const language = formatter.resolvedOptions().locale.split("-")[0];
  const placeholderParts = PLACEHOLDER_PARTS_BY_LANGUAGE[language] ?? DEFAULT_PLACEHOLDER_PARTS;
  const partOrder: DatePart[] = [];
  const localizedPattern = new RegExp(
    `^\\s*${parts
      .map((part) => {
        if (isDatePart(part.type)) {
          partOrder.push(part.type);
          return part.type === "year" ? "(\\d{4})" : "(\\d{1,2})";
        }
        return escapeRegExp(part.value);
      })
      .join("")}\\s*$`,
  );
  const completeLocalizedPattern = new RegExp(
    `^\\s*${parts
      .map((part) => {
        if (isDatePart(part.type)) {
          return part.type === "year" ? "(\\d{4})" : "(\\d{2})";
        }
        return escapeRegExp(part.value);
      })
      .join("")}\\s*$`,
  );

  return {
    format: (date: Date) => formatter.format(date),
    parse: (value: string) => {
      const isoDate = parseDate(value);
      if (isoDate) {
        return isoDate;
      }

      const match = localizedPattern.exec(value.replace(BIDI_MARK_PATTERN, ""));
      if (!match) {
        return undefined;
      }

      const parsedParts: Partial<Record<DatePart, number>> = {};
      partOrder.forEach((part, index) => {
        parsedParts[part] = Number(match[index + 1]);
      });

      const { year, month, day } = parsedParts;
      if (year === undefined || month === undefined || day === undefined) {
        return undefined;
      }

      const date = new Date(year, month - 1, day);
      if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) {
        return undefined;
      }
      return date;
    },
    isComplete: (value: string) =>
      ISO_DATE_PATTERN.test(value) || completeLocalizedPattern.test(value.replace(BIDI_MARK_PATTERN, "")),
    placeholder: parts
      .map((part) => (isDatePart(part.type) ? placeholderParts[part.type] : part.value))
      .join("")
      .replace(BIDI_MARK_PATTERN, ""),
  };
};

const formatInputValue = (value: string, localizedDateFormat: ReturnType<typeof createLocalizedDateFormat>) => {
  const date = parseDate(value);
  return date ? localizedDateFormat.format(date) : value;
};

type ValidationError = "invalid" | "min" | "max";
type CloseFocusTarget = "input" | "none";

type InputValidation = {
  date?: Date;
  error?: ValidationError;
};

const validateInputValue = (
  candidate: string,
  localizedDateFormat: ReturnType<typeof createLocalizedDateFormat>,
  minDate?: Date,
  maxDate?: Date,
): InputValidation => {
  const date = localizedDateFormat.parse(candidate);
  if (!date) {
    return { error: "invalid" };
  }
  if (minDate && date < minDate) {
    return { error: "min" };
  }
  if (maxDate && date > maxDate) {
    return { error: "max" };
  }
  return { date };
};

const DatePickerFieldContainer = styled(Field)`
  position: relative;
  min-width: 10rem;
`;

const ValidationMessage = styled(FieldMessage)`
  position: static;
  max-width: 100%;
  overflow-wrap: anywhere;
`;

const DateInputControl = styled(Control)`
  .date-picker-calendar {
    pointer-events: none;
  }

  .input:disabled ~ .icon {
    opacity: 0.5;
  }
`;

const PopoverContent = styled(RadixPopover.Content)`
  z-index: 1020;
  padding: 0.75rem;
  border: var(--scm-border, 1px solid #dbdbdb);
  border-radius: 6px;
  background-color: var(--scm-secondary-background, #fff);
  box-shadow:
    0 0.5em 1em -0.125em rgb(10 10 10 / 10%),
    0 0 0 1px rgb(10 10 10 / 2%);

  .rdp-root {
    --rdp-accent-color: var(--scm-primary-color, #00d1df);
    margin: 0;
  }

  .rdp-day_button:hover:not(:disabled) {
    background-color: var(--scm-column-selection, #effafb);
    color: var(--scm-primary-text);
  }

  .rdp-weekday {
    text-align: center;
  }

  .rdp-selected .rdp-day_button {
    color: var(--scm-secondary-least-color);
    background-color: var(--scm-secondary-text);
    border-color: var(--scm-secondary-text);
  }

  .rdp-selected.rdp-today .rdp-day_button {
    color: var(--scm-secondary-least-color);
  }

  .rdp-dropdown_root .rdp-chevron {
    fill: var(--scm-secondary-text);
  }

  .rdp-today .rdp-day_button {
    border: 1px solid var(--scm-secondary-text);
    color: var(--scm-secondary-text);
  }
`;

export type DatePickerProps = {
  label: string;
  value?: string;
  onChange: (value: string) => void;
  min?: string;
  max?: string;
  /** Label of the field providing `min`, used to suggest changing that field when the bound is violated. */
  minSourceLabel?: string;
  /** Label of the field providing `max`, used to suggest changing that field when the bound is violated. */
  maxSourceLabel?: string;
  className?: string;
  id?: string;
  name?: string;
  disabled?: boolean;
  testId?: string;
};

/**
 * Date input with a browser-independent calendar popover.
 *
 * Values use the ISO date format `yyyy-MM-dd`. The visible value and manually entered dates use the browser's regional
 * date format; ISO input remains accepted.
 *
 * @beta
 * @since 4.0.0
 */
const DatePickerField: FC<DatePickerProps> = ({
  label,
  value = "",
  onChange,
  min,
  max,
  minSourceLabel,
  maxSourceLabel,
  className,
  id,
  name,
  disabled,
  testId,
}) => {
  const { t, i18n } = useTranslation("commons");
  const inputId = useAriaId(id ?? testId);
  const errorId = `${inputId}-error`;
  const calendarId = `${inputId}-calendar`;
  const inputRef = useRef<HTMLInputElement>(null);
  const inputControlRef = useRef<HTMLDivElement>(null);
  const calendarRef = useRef<HTMLDivElement>(null);
  const focusCalendarOnOpenRef = useRef(false);
  const closeFocusTargetRef = useRef<CloseFocusTarget | undefined>(undefined);
  const [open, setOpen] = useState(false);
  const [validationError, setValidationError] = useState<ValidationError>();
  const localizedDateFormat = useMemo(() => createLocalizedDateFormat(), []);
  const [inputValue, setInputValue] = useState(() => formatInputValue(value, localizedDateFormat));
  const selected = useMemo(() => parseDate(value), [value]);
  const [pendingDate, setPendingDate] = useState<Date | undefined>(selected);
  const minDate = useMemo(() => parseDate(min), [min]);
  const maxDate = useMemo(() => parseDate(max), [max]);
  const [month, setMonth] = useState(() => selected ?? maxDate ?? minDate ?? new Date());
  const locale = chooseLocale(i18n.language, i18n.languages);

  useEffect(() => {
    setInputValue(formatInputValue(value, localizedDateFormat));
    setPendingDate(selected);
    setValidationError(undefined);
    if (selected) {
      setMonth(selected);
    }
  }, [value, localizedDateFormat, selected]);

  const applyInputValue = (candidate: string, showError = false) => {
    if (!candidate) {
      setValidationError(undefined);
      setPendingDate(undefined);
      onChange("");
      return true;
    }

    const result = validateInputValue(candidate, localizedDateFormat, minDate, maxDate);
    if (result.date) {
      const date = result.date;
      setValidationError(undefined);
      setPendingDate(date);
      setInputValue(localizedDateFormat.format(date));
      onChange(formatDate(date));
      return true;
    }
    if (showError) {
      setValidationError(result.error);
    }
    return false;
  };

  const handleInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    const candidate = event.target.value;
    setInputValue(candidate);
    setValidationError(undefined);
    if (!candidate && open) {
      closeFocusTargetRef.current = "input";
      setOpen(false);
    }
    if (!candidate || localizedDateFormat.isComplete(candidate)) {
      applyInputValue(candidate);
    }
  };

  const handleInputKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "ArrowDown" && !event.altKey && !event.ctrlKey && !event.metaKey && !event.shiftKey) {
      event.preventDefault();
      focusCalendarOnOpenRef.current = true;
      if (open) {
        const selectedDayButton = selected
          ? calendarRef.current?.querySelector<HTMLButtonElement>(
              `[data-day="${formatDate(selected)}"] .rdp-day_button:not(:disabled)`,
            )
          : undefined;
        const fallbackDayButton = calendarRef.current?.querySelector<HTMLButtonElement>(
          '[data-today] .rdp-day_button:not(:disabled), .rdp-day_button[tabindex="0"]:not(:disabled), .rdp-day_button:not(:disabled)',
        );
        (selectedDayButton ?? fallbackDayButton)?.focus();
      } else {
        setOpen(true);
      }
    } else if (event.key === "Tab" && open) {
      closeFocusTargetRef.current = "none";
      setOpen(false);
    } else if (event.key === "Escape" && open) {
      event.preventDefault();
      closeFocusTargetRef.current = "input";
      setOpen(false);
    } else if (event.key === "Enter") {
      applyInputValue(inputValue, true);
    }
  };

  useEffect(() => {
    if (validationError !== "min" && validationError !== "max") {
      return;
    }

    const result = validateInputValue(inputValue, localizedDateFormat, minDate, maxDate);
    if (result.date) {
      setInputValue(localizedDateFormat.format(result.date));
      setValidationError(undefined);
      onChange(formatDate(result.date));
    } else if (result.error === "min" || result.error === "max") {
      setValidationError(result.error);
    }
  }, [inputValue, localizedDateFormat, maxDate, minDate, onChange, validationError]);

  const errorMessage = (() => {
    if (validationError === "invalid") {
      return t("datePicker.validation.invalid", { label, format: localizedDateFormat.placeholder });
    }
    if (validationError === "min" && minDate) {
      return t("datePicker.validation.min", {
        label,
        date: localizedDateFormat.format(minDate),
      });
    }
    if (validationError === "max" && maxDate) {
      return t("datePicker.validation.max", {
        label,
        date: localizedDateFormat.format(maxDate),
      });
    }
    return undefined;
  })();

  const disabledDates = [minDate ? { before: minDate } : undefined, maxDate ? { after: maxDate } : undefined].filter(
    (date): date is { before: Date } | { after: Date } => Boolean(date),
  );
  return (
    <DatePickerFieldContainer className={className}>
      <Label htmlFor={inputId}>{label}</Label>
      <RadixPopover.Root
        open={open}
        onOpenChange={(nextOpen) => {
          if (nextOpen) {
            focusCalendarOnOpenRef.current = true;
          }
          setOpen(nextOpen);
        }}
      >
        <RadixPopover.Anchor asChild>
          <DateInputControl
            ref={inputControlRef}
            className={inputValue ? "is-expanded" : "is-expanded has-icons-right"}
          >
            <Input
              ref={inputRef}
              id={inputId}
              name={name}
              type="search"
              value={inputValue}
              variant={errorMessage ? "danger" : undefined}
              aria-invalid={errorMessage ? true : undefined}
              aria-description={t("datePicker.formatHint", {
                defaultValue: "Input format {{format}}",
                format: localizedDateFormat.placeholder.toUpperCase(),
              })}
              aria-describedby={errorMessage ? errorId : undefined}
              aria-haspopup="dialog"
              aria-expanded={open}
              aria-controls={calendarId}
              onChange={handleInputChange}
              onKeyDown={handleInputKeyDown}
              onClick={() => {
                focusCalendarOnOpenRef.current = false;
                setOpen(true);
              }}
              onBlur={() => applyInputValue(inputValue, true)}
              placeholder={localizedDateFormat.placeholder}
              inputMode="numeric"
              autoComplete="off"
              disabled={disabled}
              testId={testId}
            />
            {!inputValue ? (
              <Icon className="date-picker-calendar is-small is-right has-text-inherit">calendar-alt</Icon>
            ) : null}
          </DateInputControl>
        </RadixPopover.Anchor>
        <RadixPopover.Portal>
          <PopoverContent
            sideOffset={4}
            align="start"
            collisionPadding={8}
            onOpenAutoFocus={(event) => {
              if (!focusCalendarOnOpenRef.current) {
                event.preventDefault();
              }
            }}
            onEscapeKeyDown={() => {
              closeFocusTargetRef.current = "input";
            }}
            onInteractOutside={(event) => {
              if (inputControlRef.current?.contains(event.target as Node)) {
                event.preventDefault();
              } else {
                closeFocusTargetRef.current = "none";
              }
            }}
            onCloseAutoFocus={(event) => {
              const focusTarget = closeFocusTargetRef.current;
              closeFocusTargetRef.current = undefined;
              if (focusTarget === "input") {
                event.preventDefault();
                setTimeout(() => inputRef.current?.focus());
              } else if (focusTarget === "none") {
                event.preventDefault();
              }
            }}
          >
            <div ref={calendarRef}>
              <DayPicker
                id={calendarId}
                role="dialog"
                aria-label={label}
                mode="single"
                selected={pendingDate}
                month={month}
                onMonthChange={setMonth}
                disabled={disabledDates}
                startMonth={minDate}
                endMonth={maxDate}
                locale={locale}
                labels={{
                  labelPrevious: () => t("datePicker.previousMonth", { defaultValue: "Go to the previous month" }),
                  labelNext: () => t("datePicker.nextMonth", { defaultValue: "Go to the next month" }),
                  labelMonthDropdown: () => t("datePicker.monthDropdown", { defaultValue: "Choose a month" }),
                  labelYearDropdown: () => t("datePicker.yearDropdown", { defaultValue: "Choose a year" }),
                }}
                captionLayout="dropdown"
                navLayout="after"
                fixedWeeks
                autoFocus={focusCalendarOnOpenRef.current}
                footer={
                  <div className="is-sr-only" role="status" aria-atomic="true">
                    {pendingDate ? pendingDate.toLocaleDateString() : t("datePicker.selectDate")}
                  </div>
                }
                onSelect={(date) => {
                  const nextValue = date ? formatDate(date) : "";
                  setPendingDate(date);
                  setInputValue(date ? localizedDateFormat.format(date) : "");
                  setValidationError(undefined);
                  if (date) {
                    setMonth(date);
                  }
                  onChange(nextValue);
                  closeFocusTargetRef.current = "input";
                  setOpen(false);
                }}
              />
            </div>
          </PopoverContent>
        </RadixPopover.Portal>
      </RadixPopover.Root>
      {errorMessage ? (
        <ValidationMessage id={errorId} variant="danger">
          {errorMessage}
        </ValidationMessage>
      ) : null}
    </DatePickerFieldContainer>
  );
};

export default DatePickerField;
