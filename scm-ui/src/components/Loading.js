//@flow
import React from "react";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import Image from "../images/loading.svg";

const styles = {
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
  classes: any
};

class Loading extends React.Component<Props> {
  render() {
    const { t, classes } = this.props;
    return (
      <div className={classes.wrapper}>
        <div className={classes.loading}>
          <img className={classes.image} src={Image} alt={t("loading.alt")} />
        </div>
      </div>
    );
  }
}

export default injectSheet(styles)(translate("commons")(Loading));
