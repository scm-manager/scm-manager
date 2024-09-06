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

import { useTranslation } from "react-i18next";
import { enUS, de, es } from "date-fns/locale";
import { formatDistance, format, Locale, parseISO } from "date-fns";

type LocaleMap = {
  [key: string]: Locale;
};

export const supportedLocales: LocaleMap = {
  enUS,
  en: enUS,
  de,
  es,
};

type Options = {
  addSuffix: boolean;
  locale: Locale;
  timeZone?: string;
};

export const chooseLocale = (language: string, languages?: readonly string[]) => {
  for (const lng of languages || []) {
    const locale = supportedLocales[lng];
    if (locale) {
      return locale;
    }
  }

  const locale = supportedLocales[language];
  if (locale) {
    return locale;
  }

  return enUS;
};

export type DateInput = Date | string;

export type DateProps = {
  date?: DateInput;
  timeZone?: string;

  /**
   * baseDate is the date from which the distance is calculated,
   * default is the current time (new Date()). This property
   * is required to keep snapshots tests green over the time on
   * ci server.
   */
  baseDate?: DateInput;
};

const createOptions = (locale: Locale, timeZone?: string) => {
  const options: Options = {
    addSuffix: true,
    locale,
  };
  if (timeZone) {
    options.timeZone = timeZone;
  }
  return options;
};

const createBaseDate = (baseDate?: DateInput) => {
  if (baseDate) {
    return toDate(baseDate);
  }
  return new Date();
};

const toDate = (value: DateInput): Date => {
  if (value instanceof Date) {
    return value;
  }
  return parseISO(value);
};

const useDateFormatter = ({ date, baseDate, timeZone }: DateProps) => {
  const { i18n } = useTranslation();
  if (!date) {
    return null;
  }

  const isoDate = toDate(date);
  const base = createBaseDate(baseDate);

  const locale = chooseLocale(i18n.language, i18n.languages);
  const options = createOptions(locale, timeZone);
  return {
    formatShort() {
      return format(isoDate, "yyyy-MM-dd", options);
    },
    formatFull() {
      return format(isoDate, "yyyy-MM-dd HH:mm:ss", options);
    },
    formatDistance() {
      return formatDistance(isoDate, base, options);
    },
  };
};

export default useDateFormatter;
