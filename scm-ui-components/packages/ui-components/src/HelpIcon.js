//@flow
import React from "react";
import classNames from "classnames";

type Props = {
};

class HelpIcon extends React.Component<Props> {
  render() {
    return <i className={classNames("fa fa-question has-text-info")} />
  }
}

export default HelpIcon;
