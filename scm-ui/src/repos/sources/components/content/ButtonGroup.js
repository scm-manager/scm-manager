// @flow
import React from "react";
import { translate } from "react-i18next";
import { Button } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  historyIsSelected: boolean,
  showHistory: boolean => void
};

class ButtonGroup extends React.Component<Props> {
  showHistory = () => {
    this.props.showHistory(true);
  };

  showSources = () => {
    this.props.showHistory(false);
  };

  render() {
    const { t, historyIsSelected } = this.props;

    let sourcesColor = "";
    let historyColor = "";

    if (historyIsSelected) {
      historyColor = "link is-selected";
    } else {
      sourcesColor = "link is-selected";
    }

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
      <div className="buttons has-addons">
        <Button
          label={sourcesLabel}
          color={sourcesColor}
          action={this.showSources}
        />
        <Button
          label={historyLabel}
          color={historyColor}
          action={this.showHistory}
        />
      </div>
    );
  }
}

export default translate("repos")(ButtonGroup);
