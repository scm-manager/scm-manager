//@flow

import React from "react";
import { translate } from "react-i18next";
import { connect } from "react-redux";

type Props = {
  classes: any,
  t: string => string
};

class FileHistory extends React.Component<Props> {
  componentDidMount() {}

  render() {
    return "History";
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {};

export default connect(mapStateToProps)(translate("repos")(FileHistory));
