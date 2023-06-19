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
