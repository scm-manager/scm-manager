import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Links, Link } from "@scm-manager/ui-types";
import { apiClient, SubmitButton, Loading, ErrorNotification } from "../";
import { FormEvent } from "react";

type RenderProps = {
  readOnly: boolean;
  initialConfiguration: ConfigurationType;
  onConfigurationChange: (p1: ConfigurationType, p2: boolean) => void;
};

type Props = WithTranslation & {
  link: string;
  render: (props: RenderProps) => any; // ???
};

type ConfigurationType = {
  _links: Links;
} & object;

type State = {
  error?: Error;
  fetching: boolean;
  modifying: boolean;
  contentType?: string | null;
  configChanged: boolean;

  configuration?: ConfigurationType;
  modifiedConfiguration?: ConfigurationType;
  valid: boolean;
};

/**
 * GlobalConfiguration uses the render prop pattern to encapsulate the logic for
 * synchronizing the configuration with the backend.
 */
class Configuration extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      fetching: true,
      modifying: false,
      configChanged: false,
      valid: false
    };
  }

  componentDidMount() {
    const { link } = this.props;

    apiClient
      .get(link)
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

  loadConfig = (configuration: ConfigurationType) => {
    this.setState({
      configuration,
      fetching: false,
      error: undefined
    });
  };

  getModificationUrl = (): string | undefined => {
    const { configuration } = this.state;
    if (configuration) {
      const links = configuration._links;
      if (links && links.update) {
        const link = links.update as Link;
        return link.href;
      }
    }
  };

  isReadOnly = (): boolean => {
    const modificationUrl = this.getModificationUrl();
    return !modificationUrl;
  };

  configurationChanged = (configuration: ConfigurationType, valid: boolean) => {
    this.setState({
      modifiedConfiguration: configuration,
      valid
    });
  };

  modifyConfiguration = (event: FormEvent) => {
    event.preventDefault();

    this.setState({
      modifying: true
    });

    const { modifiedConfiguration } = this.state;

    const modificationUrl = this.getModificationUrl();
    if (modificationUrl) {
      apiClient
        .put(modificationUrl, modifiedConfiguration, this.getContentType())
        .then(() =>
          this.setState({
            modifying: false,
            configChanged: true,
            valid: false
          })
        )
        .catch(this.handleError);
    } else {
      this.setState({
        error: new Error("no modification link available")
      });
    }
  };

  renderConfigChangedNotification = () => {
    if (this.state.configChanged) {
      return (
        <div className="notification is-primary">
          <button
            className="delete"
            onClick={() =>
              this.setState({
                configChanged: false
              })
            }
          />
          {this.props.t("config.form.submit-success-notification")}
        </div>
      );
    }
    return null;
  };

  render() {
    const { t } = this.props;
    const { fetching, error, configuration, modifying, valid } = this.state;

    if (error) {
      return <ErrorNotification error={error} />;
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
        <>
          {this.renderConfigChangedNotification()}
          <form onSubmit={this.modifyConfiguration}>
            {this.props.render(renderProps)}
            <hr />
            <SubmitButton label={t("config.form.submit")} disabled={!valid || readOnly} loading={modifying} />
          </form>
        </>
      );
    }
  }
}

export default withTranslation("config")(Configuration);
