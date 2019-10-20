import React from "react";
import { translate, InjectedI18nProps } from "react-i18next";
import { formatDistance, format, parseISO, Locale } from "date-fns";
import { enUS, de, es } from "date-fns/locale";
import styled from "styled-components";

type LocaleMap = {
  [key: string]: Locale
};

type DateInput = Date | string;

const supportedLocales: LocaleMap = {
  enUS,
  de,
  es
};

type Props = InjectedI18nProps & {
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

type Options = {
  addSuffix: boolean;
  locale: Locale;
  timeZone?: string;
};

const DateElement = styled.time`
  border-bottom: 1px dotted rgba(219, 219, 219);
  cursor: help;
`;

class DateFromNow extends React.Component<Props> {
  getLocale = (): Locale => {
    const { i18n } = this.props;
    const locale = supportedLocales[i18n.language];
    if (!locale) {
      return enUS;
    }
    return locale;
  };

  createOptions = () => {
    const { timeZone } = this.props;
    const options: Options = {
      addSuffix: true,
      locale: this.getLocale()
    };
    if (timeZone) {
      options.timeZone = timeZone;
    }
    return options;
  };

  toDate = (value: DateInput): Date => {
    if (value instanceof Date) {
      return value as Date;
    }
    return parseISO(value);
  }

  getBaseDate = () => {
    const { baseDate } = this.props;
    if (baseDate) {
      return this.toDate(baseDate);
    }
    return new Date();
  };

  render() {
    const { date } = this.props;
    if (date) {
      const isoDate = this.toDate(date);
      const options = this.createOptions();
      const distance = formatDistance(isoDate, this.getBaseDate(), options);
      const formatted = format(isoDate, "yyyy-MM-dd HH:mm:ss", options);
      return <DateElement title={formatted}>{distance}</DateElement>;
    }
    return null;
  }
}

export default translate()(DateFromNow);
