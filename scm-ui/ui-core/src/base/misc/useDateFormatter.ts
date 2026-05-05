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
import { de, enUS, es } from "date-fns/locale";
import { format, formatDistance, Locale, parseISO } from "date-fns";

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

const toDate = (value: DateInput): Date => {
  if (value instanceof Date) {
    return value;
  }
  return parseISO(value);
};

export const useDateFormatter = ({ date, timeZone }: DateProps) => {
  const { i18n } = useTranslation();
  if (!date) {
    return null;
  }

  const isoDate = toDate(date);
  const locale = chooseLocale(i18n.language, i18n.languages);
  const options = createOptions(locale, timeZone);
  return {
    formatShort() {
      return isoDate.toLocaleDateString();
    },
    formatFull() {
      return isoDate.toLocaleTimeString(undefined, {
        year: "numeric",
        month: "numeric",
        day: "numeric",
        ...options,
      });
    },
    formatIso() {
      return format(isoDate, "yyyy-MM-dd HH:mm:ss", options);
    },
    formatDistance() {
      return formatDistance(isoDate, new Date(), options);
    },
  };
};
