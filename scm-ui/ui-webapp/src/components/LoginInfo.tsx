import React from "react";
import InfoBox from "./InfoBox";
import { InfoItem } from "./InfoItem";
import LoginForm from "./LoginForm";
import { Loading } from "@scm-manager/ui-components";

type Props = {
  loginInfoLink?: string;
  loading?: boolean;
  error?: Error;
  loginHandler: (username: string, password: string) => void;
};

type LoginInfoResponse = {
  plugin?: InfoItem;
  feature?: InfoItem;
};

type State = {
  info?: LoginInfoResponse;
  loading?: boolean;
};

class LoginInfo extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: !!props.loginInfoLink
    };
  }

  fetchLoginInfo = (url: string) => {
    return fetch(url)
      .then(response => response.json())
      .then(info => {
        this.setState({
          info,
          loading: false
        });
      });
  };

  timeout = (ms: number, promise: Promise<any>) => {
    return new Promise<LoginInfoResponse>((resolve, reject) => {
      setTimeout(() => {
        reject(new Error("timeout during fetch of login info"));
      }, ms);
      promise.then(resolve, reject);
    });
  };

  componentDidMount() {
    const { loginInfoLink } = this.props;
    if (!loginInfoLink) {
      return;
    }
    this.timeout(1000, this.fetchLoginInfo(loginInfoLink)).catch(() => {
      this.setState({
        loading: false
      });
    });
  }

  createInfoPanel = (info: LoginInfoResponse) => (
    <div className="column is-7 is-offset-1 is-paddingless">
      <InfoBox item={info.feature} type="feature" />
      <InfoBox item={info.plugin} type="plugin" />
    </div>
  );

  render() {
    const { info, loading } = this.state;
    if (loading) {
      return <Loading />;
    }

    let infoPanel;
    if (info) {
      infoPanel = this.createInfoPanel(info);
    }

    return (
      <>
        <LoginForm {...this.props} />
        {infoPanel}
      </>
    );
  }
}

export default LoginInfo;
