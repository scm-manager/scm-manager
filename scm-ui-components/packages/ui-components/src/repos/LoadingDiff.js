//@flow
import React from "react";
import { apiClient } from "../apiclient";
import ErrorNotification from "../ErrorNotification";
import Loading from "../Loading";
import Diff from "./Diff";

type Props = {
  url: string,
  sideBySide: boolean
};

type State = {
  diff?: string,
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
      .then(text => {
        this.setState({
          loading: false,
          diff: text
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
      return <Diff diff={diff} />;
    }
  }

}

export default LoadingDiff;
