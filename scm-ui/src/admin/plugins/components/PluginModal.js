//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Plugin } from "@scm-manager/ui-types";
import {
  Button,
  ButtonGroup,
  Checkbox,
  Modal,
  SubmitButton
} from "@scm-manager/ui-components";

type Props = {
  plugin: Plugin,
  onSubmit: () => void,
  onClose: () => void,

  // context props
  t: string => string
};

class PluginModal extends React.Component<Props> {
  renderDependencies() {
    const { plugin, t } = this.props;

    let dependencies = null;
    if (plugin.dependencies && plugin.dependencies.length > 0) {
      dependencies = (
        <>
          {t("plugins.modal.dependency", {count: plugin.dependencies.length})}
          <ul>
            {plugin.dependencies.map((dependency, index) => {
              return <li key={index}>{dependency}</li>;
            })}
          </ul>
        </>
      );
    }
    return dependencies;
  }

  render() {
    const { onSubmit, onClose, t } = this.props;

    const body = (
      <>
        {this.renderDependencies()}
        <Checkbox
          checked={false}
          label={t("plugins.modal.restart")}
          onChange={null}
          disabled={null}
        />
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
        title={t("plugins.modal.title")}
        closeFunction={() => onClose()}
        body={body}
        footer={footer}
        active={true}
      />
    );
  }
}

export default translate("admin")(PluginModal);
