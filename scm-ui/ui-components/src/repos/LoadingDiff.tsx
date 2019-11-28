import React from "react";
import { apiClient } from "../apiclient";
import ErrorNotification from "../ErrorNotification";
// @ts-ignore
import parser from "gitdiff-parser";

import Loading from "../Loading";
import Diff from "./Diff";
import { DiffObjectProps, File } from "./DiffTypes";

type Props = DiffObjectProps & {
  url: string;
  defaultCollapse?: boolean;
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
    this.setState({loading: true});
    apiClient
      .get(url)
      .then(response => response.text())
      .then(parser.parse)
      // $FlowFixMe
      .then((diff: any) => {
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

export default LoadingDiff;
