//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Links } from "@scm-manager/ui-types";
import {
  apiClient,
  Title,
  InputField,
  Checkbox,
  SubmitButton,
  Loading,
  ErrorNotification
} from "@scm-manager/ui-components";

type Props = {
  url: string,

  // context props
  t: (string) => string
};

type Configuration = {
  repositoryDirectory?: string,
  gcExpression?: string,
  disabled: boolean,
  _links?: Links
}

type State = Configuration & {
  error?: Error,
  fetching: boolean,
  modifying: boolean
};

class GitConfiguration extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      disabled: false,
      fetching: true,
      modifying: false
    };
  }

  componentDidMount() {
    const { url } = this.props;

    // TODO capture content-type for sending

    apiClient.get(url)
      .then(response => response.json())
      .then(this.loadConfig)
      .catch(this.handleError);
  }

  handleError = (error: Error) => {
    this.setState({
      error,
      fetching: false,
      modifying: false
    });
  };

  loadConfig = (configuration: Configuration) => {
    this.setState({
      ...configuration,
      fetching: false,
      error: undefined
    });
  };

  handleInputChange = (value: string, name: string) => {
    this.setState({
      [name]: value
    });
  };

  handleCheckboxChange = (value: boolean, name: string) => {
    this.setState({
      [name]: value
    });
  };

  isValid = (): boolean => {
    const { repositoryDirectory } = this.state;
    return !!repositoryDirectory;
  };

  getModificationUrl = (): ?string => {
    const links = this.state._links;
    if (links && links.update) {
      return links.update.href;
    }
  };

  isReadOnly = (): boolean => {
    const links = this.state._links;
    return !links || !links.update;
  };

  render() {
    const { fetching, error } = this.state;

    if (error) {
      return this.renderWithFrame(<ErrorNotification error={error}/>);
    } else if (fetching) {
      return this.renderWithFrame(<Loading/>);
    }

    return this.renderForm();
  }

  renderWithFrame(child) {
    const { t } = this.props;
    return (
      <div>
        <Title title={t("scm-git-plugin.config.title")}/>
        {child}
      </div>
    );
  }

  modifyConfiguration = (event: Event) => {
    event.preventDefault();

    this.setState({ modifying: true });

    const { repositoryDirectory, gcExpression, disabled } = this.state;

    const configuration = {
      repositoryDirectory,
      gcExpression,
      disabled
    };

    apiClient.put(this.getModificationUrl(), configuration, "application/vnd.scmm-gitconfig+json;v=2")
      .then(() => this.setState({ modifying: false }))
      .catch(this.handleError);
  };

  renderForm() {
    const { repositoryDirectory, gcExpression, disabled, modifying } = this.state;

    const { t } = this.props;
    const readOnly = this.isReadOnly();

    return this.renderWithFrame(
      <form onSubmit={this.modifyConfiguration}>
        <InputField name="repositoryDirectory"
                    label={t("scm-git-plugin.config.directory")}
                    helpText={t("scm-git-plugin.config.directoryHelpText")}
                    value={repositoryDirectory}
                    onChange={this.handleInputChange}
                    disabled={readOnly}
        />
        <InputField name="gcExpression"
                    label={t("scm-git-plugin.config.gcExpression")}
                    helpText={t("scm-git-plugin.config.gcExpressionHelpText")}
                    value={gcExpression}
                    onChange={this.handleInputChange}
                    disabled={readOnly}
        />
        <Checkbox name="disabled"
                  label={t("scm-git-plugin.config.disabled")}
                  helpText={t("scm-git-plugin.config.disabledHelpText")}
                  checked={disabled}
                  onChange={this.handleCheckboxChange}
                  disabled={readOnly}
        />
        <hr/>
        <SubmitButton
          label={t("scm-git-plugin.config.submit")}
          disabled={!this.isValid() || readOnly}
          loading={modifying}
        />
      </form>
    );
  }

}

export default translate("plugins")(GitConfiguration);
