import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Links } from "@scm-manager/ui-types";
import { InputField, Checkbox } from "@scm-manager/ui-components";

type Configuration = {
  repositoryDirectory?: string;
  gcExpression?: string;
  nonFastForwardDisallowed: boolean;
  _links: Links;
};

type Props = WithTranslation & {
  initialConfiguration: Configuration;
  readOnly: boolean;

  onConfigurationChange: (p1: Configuration, p2: boolean) => void;
};

type State = Configuration & {};

class GitConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  onGcExpressionChange = (value: string) => {
    this.setState(
      {
        gcExpression: value
      },
      () => this.props.onConfigurationChange(this.state, true)
    );
  };

  onNonFastForwardDisallowed = (value: boolean) => {
    this.setState(
      {
        nonFastForwardDisallowed: value
      },
      () => this.props.onConfigurationChange(this.state, true)
    );
  };

  render() {
    const { gcExpression, nonFastForwardDisallowed } = this.state;
    const { readOnly, t } = this.props;

    return (
      <>
        <InputField
          name="gcExpression"
          label={t("scm-git-plugin.config.gcExpression")}
          helpText={t("scm-git-plugin.config.gcExpressionHelpText")}
          value={gcExpression}
          onChange={this.onGcExpressionChange}
          disabled={readOnly}
        />
        <Checkbox
          name="nonFastForwardDisallowed"
          label={t("scm-git-plugin.config.nonFastForwardDisallowed")}
          helpText={t("scm-git-plugin.config.nonFastForwardDisallowedHelpText")}
          checked={nonFastForwardDisallowed}
          onChange={this.onNonFastForwardDisallowed}
          disabled={readOnly}
        />
      </>
    );
  }
}

export default withTranslation("plugins")(GitConfigurationForm);
