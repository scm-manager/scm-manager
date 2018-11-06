//@flow
import React from "react";
import type { Links } from "@scm-manager/ui-types";
import GitConfiguration from "./GitConfiguration";
import { Route } from "react-router-dom";

type Props = {
  url: string,
  links: Links
}

class GitConfigurationRoute extends React.Component<Props> {

  render() {
    const { url, links } = this.props;

    const configLink = links["gitConfig"].href;
    return <Route path={url + "/git"}
                  component={() => <GitConfiguration url={configLink} />}
                  exact />;
  }

}

export default GitConfigurationRoute;
