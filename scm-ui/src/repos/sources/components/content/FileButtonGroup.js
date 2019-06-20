// @flow
import React from "react";
import { translate } from "react-i18next";
import { ButtonAddons, Button } from "@scm-manager/ui-components";

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
      <ButtonAddons>
        <Button
          action={this.showSources}
          className="reduced-mobile"
          color={this.color(!historyIsSelected)}
        >
          <span className="icon">
            <i className="fas fa-code" />
          </span>
          <span>{t("sources.content.sourcesButton")}</span>
        </Button>
        <Button
          action={this.showHistory}
          className="reduced-mobile"
          color={this.color(historyIsSelected)}
        >
          <span className="icon">
            <i className="fas fa-history" />
          </span>
          <span>{t("sources.content.historyButton")}</span>
        </Button>
      </ButtonAddons>
    );
  }
}

export default translate("repos")(FileButtonGroup);
