//@flow
import React from "react";
import moment from "moment";
import { translate } from "react-i18next";
import injectSheet from "react-jss";

// fix german locale
// https://momentjscom.readthedocs.io/en/latest/moment/00-use-it/07-browserify/
import "moment/locale/de";

const styles = {
  date: {
    borderBottom: "1px dotted rgba(219, 219, 219)",
    cursor: "help"
  }
};

type Props = {
  date?: string,

  // context props
  classes: any,
  i18n: any
};

class DateFromNow extends React.Component<Props> {

  render() {
    const { i18n, date, classes } = this.props;

    if (date) {
      const dateWithLocale = moment(date).locale(i18n.language);

      return (
        <time title={dateWithLocale.format()} className={classes.date}>
          {dateWithLocale.fromNow()}
        </time>
      );
    }

    return null;
  }
}

export default injectSheet(styles)(translate()(DateFromNow));
