import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { ButtonAddons, Button } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  className?: string;
  historyIsSelected: boolean;
  showHistory: (p: boolean) => void;
};

class FileButtonAddons extends React.Component<Props> {
  showHistory = () => {
    this.props.showHistory(true);
  };

  showSources = () => {
    this.props.showHistory(false);
  };

  color = (selected: boolean) => {
    return selected ? "link is-selected" : "";
  };

  render() {
    const { className, t, historyIsSelected } = this.props;

    return (
      <ButtonAddons className={className}>
        <div title={t("sources.content.sourcesButton")}>
          <Button action={this.showSources} color={this.color(!historyIsSelected)}>
            <span className="icon">
              <i className="fas fa-code" />
            </span>
          </Button>
        </div>
        <div title={t("sources.content.historyButton")}>
          <Button action={this.showHistory} color={this.color(historyIsSelected)}>
            <span className="icon">
              <i className="fas fa-history" />
            </span>
          </Button>
        </div>
      </ButtonAddons>
    );
  }
}

export default withTranslation("repos")(FileButtonAddons);
