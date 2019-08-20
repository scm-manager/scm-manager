//@flow
import React from "react";
import { compose } from "redux";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import type { Plugin } from "@scm-manager/ui-types";
import {
  Button,
  ButtonGroup,
  Checkbox,
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
  t: string => string
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
  }
};

class PluginModal extends React.Component<Props> {
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

  render() {
    const { plugin, onSubmit, onClose, classes, t } = this.props;

    const body = (
      <>
        <div className="media">
          <div className="media-content">
            <p>{plugin.description && plugin.description}</p>
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
      </>
    );

    const footer = (
      <form onSubmit={onSubmit}>
        <ButtonGroup>
          <SubmitButton label={t("plugins.modal.install")} />
          <Button label={t("plugins.modal.abort")} action={onClose} />
        </ButtonGroup>
      </form>
    );

    return (
      <Modal
        title={t("plugins.modal.title", {
          name: plugin.displayName ? plugin.displayName : ""
        })}
        closeFunction={() => onClose()}
        body={body}
        footer={footer}
        active={true}
      />
    );
  }
}

export default compose(
  injectSheet(styles),
  translate("admin")
)(PluginModal);
