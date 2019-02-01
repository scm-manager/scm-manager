// @flow
import React from "react";
import { translate } from "react-i18next";
import { ButtonGroup } from "@scm-manager/ui-components";

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

  render() {
    const { t, historyIsSelected } = this.props;

    const sourcesLabel = (
      <>
        <span className="icon">
          <i className="fas fa-code" />
        </span>
        <span className="is-hidden-mobile">
          {t("sources.content.sourcesButton")}
        </span>
      </>
    );

    const historyLabel = (
      <>
        <span className="icon">
          <i className="fas fa-history" />
        </span>
        <span className="is-hidden-mobile">
          {t("sources.content.historyButton")}
        </span>
      </>
    );

    return (
      <ButtonGroup
        firstlabel={sourcesLabel}
        secondlabel={historyLabel}
        firstAction={this.showSources}
        secondAction={this.showHistory}
        firstIsSelected={!historyIsSelected}
      />
    );
  }
}

export default translate("repos")(FileButtonGroup);
