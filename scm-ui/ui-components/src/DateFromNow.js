//@flow
import React from "react";
import { translate } from "react-i18next";
import { formatDistance, format, parseISO } from "date-fns";
import { enUS, de, es } from "date-fns/locale";
import styled from "styled-components";

const supportedLocales = {
  enUS,
  de,
  es
};

type Props = {
  date?: string,
  timeZone?: string,

  /**
   * baseDate is the date from which the distance is calculated,
   * default is the current time (new Date()). This property
   * is required to keep snapshots tests green over the time on
   * ci server.
   */
  baseDate?: string,

  // context props
  i18n: any
};

const DateElement = styled.time`
  border-bottom: 1px dotted rgba(219, 219, 219);
  cursor: help;
`;

class DateFromNow extends React.Component<Props> {
  getLocale = () => {
    const { i18n } = this.props;
    const locale = supportedLocales[i18n.language];
    if (!locale) {
      return enUS;
    }
    return locale;
  };

  createOptions = () => {
    const { timeZone } = this.props;
    const options: Object = {
      addSuffix: true,
      locate: this.getLocale(),
    };
    if (timeZone) {
      options.timeZone = timeZone;
    }
    return options;
  };

  getBaseDate = () => {
    const { baseDate } = this.props;
    if (baseDate) {
      return parseISO(baseDate);
    }
    return new Date();
  };

  render() {
    const { date } = this.props;
    if (date) {
      const isoDate = parseISO(date);
      const options = this.createOptions();
      const distance = formatDistance(isoDate, this.getBaseDate(), options);
      const formatted = format(isoDate, "yyyy-MM-dd HH:mm:ss", options);
      return <DateElement title={formatted}>{distance}</DateElement>;
    }
    return null;
  }
}

export default translate()(DateFromNow);
