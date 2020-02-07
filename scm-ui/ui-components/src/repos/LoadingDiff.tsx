import React from "react";
import { apiClient } from "../apiclient";
import ErrorNotification from "../ErrorNotification";
// @ts-ignore
import parser from "gitdiff-parser";

import Loading from "../Loading";
import Diff from "./Diff";
import { DiffObjectProps, File } from "./DiffTypes";
import { NotFoundError } from "../errors";
import { Notification } from "../index";
import { withTranslation, WithTranslation } from "react-i18next";
import { DefaultCollapsed } from "./defaultCollapsed";

type Props = WithTranslation &
  DiffObjectProps & {
    url: string;
    defaultCollapse?: DefaultCollapsed;
  };

type State = {
  diff?: File[];
  loading: boolean;
  error?: Error;
};

class LoadingDiff extends React.Component<Props, State> {
  static defaultProps = {
    sideBySide: false
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      loading: true
    };
  }

  componentDidMount() {
    this.fetchDiff();
  }

  componentDidUpdate(prevProps: Props) {
    if (prevProps.url !== this.props.url) {
      this.fetchDiff();
    }
  }

  fetchDiff = () => {
    const { url } = this.props;
    this.setState({ loading: true });
    apiClient
      .get(url)
      .then(response => {
        const contentType = response.headers.get("Content-Type");
        if (contentType && contentType.toLowerCase() === "application/vnd.scmm-diffparsed+json;v=2") {
          return response.json().then(data => data.files);
        } else {
          return response.text().then(parser.parse);
        }
      })
      .then((diff: File[]) => {
        this.setState({
          loading: false,
          diff: diff
        });
      })
      .catch((error: Error) => {
        this.setState({
          loading: false,
          error
        });
      });
  };

  render() {
    const { diff, loading, error } = this.state;
    if (error) {
      if (error instanceof NotFoundError) {
        return <Notification type="info">{this.props.t("changesets.noChangesets")}</Notification>;
      }
      return <ErrorNotification error={error} />;
    } else if (loading) {
      return <Loading />;
    } else if (!diff) {
      return null;
    } else {
      return <Diff diff={diff} {...this.props} />;
    }
  }
}

export default withTranslation("repos")(LoadingDiff);
