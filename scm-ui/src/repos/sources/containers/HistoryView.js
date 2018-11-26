// @flow
import React from "react";
import { translate } from "react-i18next";

import { getContentType } from "./contentType";
import type { File, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  file: File,
  revision: string,
  path: string,
  classes: any,
  t: string => string
};

type State = {
  loaded: boolean,
  error?: Error
};

class HistoryView extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      loaded: false
    };
  }

  componentDidMount() {
    const { file } = this.props;
   /* getContentType(file._links.self.href)
      .then(result => {
        if (result.error) {
          this.setState({
            ...this.state,
            error: result.error,
            loaded: true
          });
        } else {
          this.setState({
            ...this.state,
            contentType: result.type,
            language: result.language,
            loaded: true
          });
        }
      })
      .catch(err => {});*/
  }

  showHistory() {
    return "Hallo";
  }

  render() {
    const { classes, file } = this.props;
    const { loaded, error } = this.state;

    if (!file || !loaded) {
      return <Loading />;
    }
    if (error) {
      return <ErrorNotification error={error} />;
    }

    const history = this.showHistory();

    return <>{history}</>;
  }
}

export default translate("repos")(HistoryView);
