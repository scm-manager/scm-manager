//@flow
import React from "react";
import ErrorNotification from "./ErrorNotification";
import { translate } from "react-i18next";

type Props = {
  error: Error,
  title: string,
  subtitle?: string,
  t: string => string
};

type State = {
  collapsed: boolean
};

class CollapsibleErrorPage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: true
    };
  }



  render() {
    const { title, error, t } = this.props;

    return (
      <section className="section">
        <div className="box column is-4 is-offset-4 container has-background-danger">
          <span onClick={() => {this.setState({collapsed: !this.state.collapsed})}}>
            <h1 className="title"> {title} </h1>
          </span>

          <ErrorNotification error={error} />
        </div>
      </section>
    );
  }
}

export default translate("plugins")(CollapsibleErrorPage);
