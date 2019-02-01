//@flow
import React from "react";

type Props = {
  displayName: string,
  url: string,
  disabled: boolean
};

class DownloadButton extends React.Component<Props> {
  render() {
    const { displayName, url, disabled } = this.props;
    return (
      <a className="button is-large is-link" href={url} disabled={disabled}>
        <span className="icon is-medium">
          <i className="fas fa-arrow-circle-down" />
        </span>
        <span>{displayName}</span>
      </a>
    );
  }
}

export default DownloadButton;
