//@flow
import React from "react";
import Button, { type ButtonProps } from "./Button";
import type {File} from "@scm-manager/ui-types";

type Props = {
  displayName: string,
  url: string
};

class DownloadButton extends React.Component<Props> {
  render() {
    const {displayName, url} = this.props;
    return (
      <a className="button is-large is-info" href={url}>
        <span className="icon is-medium">
          <i className="fas fa-arrow-circle-down" />
        </span>
        <span>{displayName}</span>
      </a>
    );
  }
}

export default DownloadButton;
