//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Links } from "@scm-manager/ui-types";

import { InputField, Checkbox } from "@scm-manager/ui-components";

type Configuration = {
  repositoryDirectory?: string,
  gcExpression?: string,
  disabled: boolean,
  _links: Links
}

type Props = {
  initialConfiguration: Configuration,
  readOnly: boolean,

  onConfigurationChange: (Configuration, boolean) => void,

  // context props
  t: (string) => string
}

type State = Configuration & {

}

class GitConfigurationForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = { ...props.initialConfiguration };
  }

  isValid = () => {
    return !!this.state.repositoryDirectory;
  };

  handleChange = (value: any, name: string) => {
    this.setState({
      [name]: value
    }, () => this.props.onConfigurationChange(this.state, this.isValid()));
  };

  render() {
    const { repositoryDirectory, gcExpression, disabled } = this.state;
    const { readOnly, t } = this.props;

    return (
      <>
        <InputField name="repositoryDirectory"
                    label={t("scm-git-plugin.config.directory")}
                    helpText={t("scm-git-plugin.config.directoryHelpText")}
                    value={repositoryDirectory}
                    onChange={this.handleChange}
                    disabled={readOnly}
        />
        <InputField name="gcExpression"
                    label={t("scm-git-plugin.config.gcExpression")}
                    helpText={t("scm-git-plugin.config.gcExpressionHelpText")}
                    value={gcExpression}
                    onChange={this.handleChange}
                    disabled={readOnly}
        />
        <Checkbox name="disabled"
                  label={t("scm-git-plugin.config.disabled")}
                  helpText={t("scm-git-plugin.config.disabledHelpText")}
                  checked={disabled}
                  onChange={this.handleChange}
                  disabled={readOnly}
        />
      </>
    );
  }

}

export default translate("plugins")(GitConfigurationForm);
