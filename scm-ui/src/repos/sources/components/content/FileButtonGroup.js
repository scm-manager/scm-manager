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
        <div title={t("sources.content.sourcesButton")}>
          <Button
            action={this.showSources}
            className="reduced"
            color={this.color(!historyIsSelected)}
          >
            <span className="icon">
              <i className="fas fa-code" />
            </span>
          </Button>
        </div>
        <div title={t("sources.content.historyButton")}>
          <Button
            action={this.showHistory}
            className="reduced"
            color={this.color(historyIsSelected)}
          >
            <span className="icon">
              <i className="fas fa-history" />
            </span>
          </Button>
        </div>
      </ButtonAddons>
    );
  }
}

export default translate("repos")(FileButtonGroup);
