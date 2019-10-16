//@flow
import React from "react";

type Props = {
  displayName: string,
  url: string,
  disabled: boolean,
  onClick?: () => void
};

class DownloadButton extends React.Component<Props> {
  render() {
    const { displayName, url, disabled, onClick } = this.props;
    const onClickOrDefault = !!onClick ? onClick : () => {};
    return (
      <a className="button is-link" href={url} disabled={disabled} onClick={onClickOrDefault}>
        <span className="icon is-medium">
          <i className="fas fa-arrow-circle-down" />
        </span>
        <span>{displayName}</span>
      </a>
    );
  }
}

export default DownloadButton;
