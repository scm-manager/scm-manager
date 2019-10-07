//@flow

import React from "react";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import Image from "./Image";

const styles = {
  minHeightContainer: {
    minHeight: "256px"
  },
  wrapper: {
    position: "relative"
  },
  loading: {
    width: "128px",
    height: "128px",

    position: "absolute",
    top: "50%",
    left: "50%",

    margin: "64px 0 0 -64px"
  },
  image: {
    width: "128px",
    height: "128px"
  }
};

type Props = {
  t: string => string,
  message?: string,
  classes: any
};

class Loading extends React.Component<Props> {
  render() {
    const { message, t, classes } = this.props;
    return (
      <div className={classes.minHeightContainer}>
        <div className={classes.wrapper}>
          <div className={classes.loading}>
            <Image
              className={classes.image}
              src="/images/loading.svg"
              alt={t("loading.alt")}
            />
            <p className="has-text-centered">{message}</p>
          </div>
        </div>
      </div>
    );
  }
}

export default injectSheet(styles)(translate("commons")(Loading));
