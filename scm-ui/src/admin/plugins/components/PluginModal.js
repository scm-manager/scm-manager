//@flow
import React from "react";
import { compose } from "redux";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import type { Plugin } from "@scm-manager/ui-types";
import {
  apiClient,
  Button,
  ButtonGroup,
  Checkbox, ErrorNotification,
  Modal,
  SubmitButton
} from "@scm-manager/ui-components";
import classNames from "classnames";

type Props = {
  plugin: Plugin,
  onSubmit: () => void,
  onClose: () => void,

  // context props
  classes: any,
  t: (key: string, params?: Object) => string
};

type State = {
  loading: boolean,
  error?: Error
};

const styles = {
  titleVersion: {
    marginLeft: "0.75rem"
  },
  userLabelAlignment: {
    textAlign: "left",
    marginRight: 0,
    minWidth: "5.5em"
  },
  userFieldFlex: {
    flexGrow: 4
  },
  listSpacing: {
    marginTop: "0 !important"
  },
  error: {
    marginTop: "1em"
  }
};

class PluginModal extends React.Component<Props,State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      loading: false
    };
  }

  install = (e: Event) => {
    const { plugin, onClose } = this.props;
    this.setState({
      loading: true
    });
    e.preventDefault();
    apiClient.post(plugin._links.install.href)
      .then(() => {
        this.setState({
          loading: false,
          error: undefined
        }, onClose);
      })
      .catch(error => {
        this.setState({
          loading: false,
          error: error
        });
      });
  };

  footer = () => {
    const { onClose, t } = this.props;
    const { loading, error } = this.state;
    return (
      <form>
        <ButtonGroup>
          <SubmitButton label={t("plugins.modal.install")} loading={loading} action={this.install} disabled={!!error} />
          <Button label={t("plugins.modal.abort")} action={onClose} />
        </ButtonGroup>
      </form>
    );
  };

  renderDependencies() {
    const { plugin, classes, t } = this.props;

    let dependencies = null;
    if (plugin.dependencies && plugin.dependencies.length > 0) {
      dependencies = (
        <>
          <strong>{t("plugins.modal.dependencyNotification")}</strong>
          <div className="field is-horizontal">
            <div
              className={classNames(
                classes.userLabelAlignment,
                "field-label is-inline-flex"
              )}
            >
              {t("plugins.modal.dependencies")}:
            </div>
            <div
              className={classNames(
                classes.userFieldFlex,
                "field-body is-inline-flex"
              )}
            >
              <ul className={classes.listSpacing}>
                {plugin.dependencies.map((dependency, index) => {
                  return <li key={index}>{dependency}</li>;
                })}
              </ul>
            </div>
          </div>
        </>
      );
    }
    return dependencies;
  }

  renderError = () => {
    const { classes } = this.props;
    const { error } = this.state;
    if (error) {
      return (
        <div className={classes.error}>
          <ErrorNotification error={error} />
        </div>
      );
    }
    return null;
  };

  render() {

    const { plugin, onClose, classes, t } = this.props;

    const body = (
      <>
        <div className="media">
          <div className="media-content">
            <p>{plugin.description}</p>
          </div>
        </div>
        <div className="media">
          <div className="media-content">
            <div className="field is-horizontal">
              <div
                className={classNames(
                  classes.userLabelAlignment,
                  "field-label is-inline-flex"
                )}
              >
                {t("plugins.modal.author")}:
              </div>
              <div
                className={classNames(
                  classes.userFieldFlex,
                  "field-body is-inline-flex"
                )}
              >
                {plugin.author}
              </div>
            </div>
            <div className="field is-horizontal">
              <div
                className={classNames(
                  classes.userLabelAlignment,
                  "field-label is-inline-flex"
                )}
              >
                {t("plugins.modal.version")}:
              </div>
              <div
                className={classNames(
                  classes.userFieldFlex,
                  "field-body is-inline-flex"
                )}
              >
                {plugin.version}
              </div>
            </div>

            {this.renderDependencies()}
          </div>
        </div>
        <div className="media">
          <div className="media-content">
            <Checkbox
              checked={false}
              label={t("plugins.modal.restart")}
              onChange={null}
              disabled={null}
            />
          </div>
        </div>
        {this.renderError()}
      </>
    );

    return (
      <Modal
        title={t("plugins.modal.title", {
          name: plugin.displayName ? plugin.displayName : plugin.name
        })}
        closeFunction={() => onClose()}
        body={body}
        footer={this.footer()}
        active={true}
      />
    );
  }
}

export default compose(
  injectSheet(styles),
  translate("admin")
)(PluginModal);
