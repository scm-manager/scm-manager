//@flow
import React from "react";

type Props = {
  displayName: string,
  url: string
};

class DownloadButton extends React.Component<Props> {
  render() {
    const { displayName, url } = this.props;
    return (
      <a className="button is-large is-link" href={url}>
        <span className="icon is-medium">
          <i className="fas fa-arrow-circle-down" />
        </span>
        <span>{displayName}</span>
      </a>
    );
  }
}

export default DownloadButton;
