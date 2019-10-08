//@flow
import React from "react";
import moment from "moment";
import { translate } from "react-i18next";
import styled from "styled-components";

// fix german locale
// https://momentjscom.readthedocs.io/en/latest/moment/00-use-it/07-browserify/
import "moment/locale/de";

type Props = {
  date?: string,

  // context props
  i18n: any
};

const Date = styled.time`
  border-bottom: 1px dotted rgba(219, 219, 219);
  cursor: help;
`;

class DateFromNow extends React.Component<Props> {
  render() {
    const { i18n, date } = this.props;

    if (date) {
      const dateWithLocale = moment(date).locale(i18n.language);

      return (
        <Date title={dateWithLocale.format()}>
          {dateWithLocale.fromNow()}
        </Date>
      );
    }

    return null;
  }
}

export default translate()(DateFromNow);
