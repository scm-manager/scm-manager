//@flow
import React from "react";
import type { Links } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import { InputField, Checkbox } from "@scm-manager/ui-components";

type Configuration = {
  "hgBinary": string,
  "pythonBinary": string,
  "pythonPath"?: string,
  "repositoryDirectory": string,
  "encoding": string,
  "useOptimizedBytecode": boolean,
  "showRevisionInId": boolean,
  "disabled": boolean,
  "_links": Links
};

type Props = {
  initialConfiguration: Configuration,
  readOnly: boolean,

  onConfigurationChange: (Configuration, boolean) => void,

  // context props
  t: (string) => string
}

type State = Configuration & {
  validationErrors: string[]
};

class HgConfigurationForm extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = { ...props.initialConfiguration, validationErrors: [] };
  }

  updateValidationStatus = () => {
    const requiredFields = [
      "hgBinary", "pythonBinary", "repositoryDirectory", "encoding"
    ];

    const validationErrors = [];
    for (let field of requiredFields) {
      if (!this.state[field]) {
        validationErrors.push( field );
      }
    }

    this.setState({
      validationErrors
    });
  };

  isValid = () => {
    return this.state.validationErrors.length === 0;
  };

  hasValidationError = (name: string) => {
    return this.state.validationErrors.indexOf(name) >= 0;
  };

  handleChange = (value: any, name: string) => {
    this.setState({
      [name]: value
    }, () => {
      this.updateValidationStatus();
      this.props.onConfigurationChange(this.state, this.isValid());
    });
  };

  inputField = (name: string) => {
    const { readOnly, t } = this.props;
    return <InputField
      name={ name }
      label={t("scm-hg-plugin.config." + name)}
      helpText={t("scm-hg-plugin.config." + name + "HelpText")}
      value={this.state[name]}
      onChange={this.handleChange}
      validationError={this.hasValidationError(name)}
      errorMessage={t("scm-hg-plugin.config.required")}
      disabled={readOnly}
    />;
  };

  checkbox = (name: string) => {
    const { readOnly, t } = this.props;
    return <Checkbox
      name={ name }
      label={t("scm-hg-plugin.config." + name)}
      helpText={t("scm-hg-plugin.config." + name + "HelpText")}
      checked={this.state[name]}
      onChange={this.handleChange}
      disabled={readOnly}
    />;
  };

  render() {
    return (
      <>
        {this.inputField("hgBinary")}
        {this.inputField("pythonBinary")}
        {this.inputField("pythonPath")}
        {this.inputField("repositoryDirectory")}
        {this.inputField("encoding")}
        {this.checkbox("useOptimizedBytecode")}
        {this.checkbox("showRevisionInId")}
        {this.checkbox("disabled")}
      </>
    );
  }

}

export default translate("plugins")(HgConfigurationForm);
