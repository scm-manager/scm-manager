// @flow
import React from "react";
import { translate } from "react-i18next";
import { ButtonGroup, Button } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  historyIsSelected: boolean,
  showHistory: boolean => void
};

class FileButtonGroup extends React.Component<Props> {
  showHistory = () => {
    this.props.showHistory(true);
  };

  showSources = () => {
    this.props.showHistory(false);
  };

  color = (selected: boolean) => {
    return selected ? "link is-selected" : null;
  };

  render() {
    const { t, historyIsSelected } = this.props;

    return (
      <ButtonGroup>
        <Button action={this.showSources} color={ this.color(!historyIsSelected) }>
          <span className="icon">
            <i className="fas fa-code"/>
          </span>
            <span className="is-hidden-mobile">
            {t("sources.content.sourcesButton")}
          </span>
        </Button>
        <Button action={this.showHistory} color={ this.color(historyIsSelected) }>
          <span className="icon">
            <i className="fas fa-history"/>
          </span>
            <span className="is-hidden-mobile">
            {t("sources.content.historyButton")}
          </span>
          </Button>
      </ButtonGroup>
    );
  }
}

export default translate("repos")(FileButtonGroup);
