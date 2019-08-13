//@flow
import React from "react";
import InfoBox from "./InfoBox";
import type { InfoItem } from "./InfoItem";

type Props = {
  loginInfoLink: string
};

type State = {
  plugin?: InfoItem,
  feature?: InfoItem,
  error?: Error
};

class LoginInfo extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount() {
    const { loginInfoLink } = this.props;
    fetch(loginInfoLink)
      .then(response => response.json())
      .then(info => {
        this.setState({
          plugin: info.plugin,
          feature: info.feature,
          error: undefined
        });
      })
      .catch(error => {
        this.setState({
          error
        });
      });
  }

  render() {
    const { plugin, feature, error } = this.state;
    return (
      <div className="column is-7 is-offset-1 is-paddingless">
        <InfoBox item={feature} type="feature" error={error} />
        <InfoBox item={plugin} type="plugin" error={error} />
      </div>
    );
  }

}

export default LoginInfo;


