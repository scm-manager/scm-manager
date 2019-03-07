//@flow
import React from "react";
import { apiClient } from "../apiclient";
import ErrorNotification from "../ErrorNotification";
import parser from "gitdiff-parser";

import Loading from "../Loading";
import Diff from "./Diff";
import type {DiffObjectProps} from "./DiffTypes";

type Props = DiffObjectProps & {
  url: string
};

type State = {
  diff?: any,
  loading: boolean,
  error?: Error
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
    if(prevProps.url !== this.props.url){
      this.fetchDiff();
    }
  }

  fetchDiff = () => {
    const { url } = this.props;
    apiClient
      .get(url)
      .then(response => response.text())
      .then(parser.parse)
      .then(diff => {
        this.setState({
          loading: false,
          diff: diff
        });
      })
      .catch(error => {
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
    } else if(!diff){
        return null;
    }
    else {
      return <Diff diff={diff} {...this.props} />;
    }
  }

}

export default LoadingDiff;
