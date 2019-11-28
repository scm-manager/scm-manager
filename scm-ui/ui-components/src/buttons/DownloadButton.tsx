import React from "react";

type Props = {
  displayName: string;
  url: string;
  disabled: boolean;
  onClick?: () => void;
};

class DownloadButton extends React.Component<Props> {
  render() {
    const { displayName, url, disabled, onClick } = this.props;
    const onClickOrDefault = !!onClick ? onClick : () => {};
    return (
      <>
        {/*
        we have to ignore the next line, 
        because jsx a does not the custom disabled attribute
        but bulma does.
        // @ts-ignore */}
        <a className="button is-link" href={url} disabled={disabled} onClick={onClickOrDefault}>
          <span className="icon is-medium">
            <i className="fas fa-arrow-circle-down" />
          </span>
          <span>{displayName}</span>
        </a>
      </>
    );
  }
}

export default DownloadButton;
