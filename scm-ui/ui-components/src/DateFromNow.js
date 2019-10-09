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
    const locale = this.getLocale();
    return {
      locale
    };
  };

  render() {
    const { date } = this.props;
    if (date) {
      const isoDate = parseISO(date);
      const options = this.createOptions();
      const distance = formatDistance(isoDate, new Date(), options);
      const formatted = format(isoDate, "yyyy-MM-dd HH:mm:ss", options);
      return <DateElement title={formatted}>{distance}</DateElement>;
    }
    return null;
  }
}

export default translate()(DateFromNow);
