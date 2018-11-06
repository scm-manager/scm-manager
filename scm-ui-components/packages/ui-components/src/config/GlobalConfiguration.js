//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Links } from "@scm-manager/ui-types";
import {
  apiClient,
  SubmitButton,
  Loading,
  ErrorNotification
} from "../";

type RenderProps = {
  readOnly: boolean,
  initialConfiguration: Configuration,
  onConfigurationChange: (Configuration, boolean) => void
};

type Props = {
  link: string,
  render: (props: RenderProps) => any, // ???

  // context props
  t: (string) => string
};

type Configuration =  {
  _links: Links
} & Object;

type State = {
  error?: Error,
  fetching: boolean,
  modifying: boolean,
  contentType?: string,

  configuration?: Configuration,
  modifiedConfiguration?: Configuration,
  valid: boolean
};

/**
 * GlobalConfiguration uses the render prop pattern to encapsulate the logic for
 * synchronizing the configuration with the backend.
 */
class GlobalConfiguration extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      fetching: true,
      modifying: false,
      valid: false
    };
  }

  componentDidMount() {
    const { link } = this.props;

    apiClient.get(link)
      .then(this.captureContentType)
      .then(response => response.json())
      .then(this.loadConfig)
      .catch(this.handleError);
  }

  captureContentType = (response: Response) => {
    const contentType = response.headers.get("Content-Type");
    this.setState({
      contentType
    });
    return response;
  };

  getContentType = (): string => {
    const { contentType } = this.state;
    return contentType ? contentType : "application/json";
  };

  handleError = (error: Error) => {
    this.setState({
      error,
      fetching: false,
      modifying: false
    });
  };

  loadConfig = (configuration: Configuration) => {
    this.setState({
      configuration,
      fetching: false,
      error: undefined
    });
  };

  getModificationUrl = (): ?string => {
    const { configuration } = this.state;
    if (configuration) {
      const links = configuration._links;
      if (links && links.update) {
        return links.update.href;
      }
    }
  };

  isReadOnly = (): boolean => {
    const modificationUrl = this.getModificationUrl();
    return !modificationUrl;
  };

  configurationChanged = (configuration: Configuration, valid: boolean) => {
    this.setState({
      modifiedConfiguration: configuration,
      valid
    });
  };

  modifyConfiguration = (event: Event) => {
    event.preventDefault();

    this.setState({ modifying: true });

    const {modifiedConfiguration} = this.state;

    apiClient.put(this.getModificationUrl(), modifiedConfiguration, this.getContentType())
      .then(() => this.setState({ modifying: false }))
      .catch(this.handleError);
  };

  render() {
    const { t } = this.props;
    const { fetching, error, configuration, modifying, valid } = this.state;

    if (error) {
      return <ErrorNotification error={error}/>;
    } else if (fetching || !configuration) {
      return <Loading />;
    } else {
      const readOnly = this.isReadOnly();

      const renderProps: RenderProps = {
        readOnly,
        initialConfiguration: configuration,
        onConfigurationChange: this.configurationChanged
      };

      return (
        <form onSubmit={this.modifyConfiguration}>
          { this.props.render(renderProps) }
          <hr/>
          <SubmitButton
            label={t("config-form.submit")}
            disabled={!valid || readOnly}
            loading={modifying}
          />
        </form>
      );
    }
  }

}

export default translate("config")(GlobalConfiguration);
