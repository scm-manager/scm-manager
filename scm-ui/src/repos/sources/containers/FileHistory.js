//@flow

import React from "react";
import { translate } from "react-i18next";
import type { File, Repository } from "@scm-manager/ui-types";
import {
  DateFromNow,
  ErrorNotification,
  Loading
} from "@scm-manager/ui-components";
import { connect } from "react-redux";
import ImageViewer from "../components/content/ImageViewer";
import SourcecodeViewer from "../components/content/SourcecodeViewer";
import DownloadViewer from "../components/content/DownloadViewer";
import FileSize from "../components/FileSize";
import injectSheet from "react-jss";
import classNames from "classnames";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { getContentType } from "./contentType";

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
