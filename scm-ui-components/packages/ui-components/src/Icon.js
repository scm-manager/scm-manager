//@flow
import React from "react";
import classNames from "classnames";

type Props = {
  title?: string,
  name: string
}

export default class Icon extends React.Component<Props> {

  render() {
    const { title, name } = this.props;
    if(title) {
      return (
        <i title={title} className={classNames("is-icon", "fas", "fa-fw", "fa-" + name)}/>
      );
    }
    return (
      <i className={classNames("is-icon", "fas", "fa-" + name)}/>
    );
  }

}

