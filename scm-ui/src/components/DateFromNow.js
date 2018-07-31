//@flow
import React from "react";
import moment from "moment";
import { translate } from "react-i18next";

type Props = {
  date?: string,

  // context props
  i18n: any
};

class DateFromNow extends React.Component<Props> {
  static format(locale: string, date?: string) {
    let fromNow = "";
    if (date) {
      fromNow = moment(date)
        .locale(locale)
        .fromNow();
    }
    return fromNow;
  }

  render() {
    const { i18n, date } = this.props;

    const fromNow = DateFromNow.format(i18n.language, date);
    return <span>{fromNow}</span>;
  }
}

export default translate()(DateFromNow);
