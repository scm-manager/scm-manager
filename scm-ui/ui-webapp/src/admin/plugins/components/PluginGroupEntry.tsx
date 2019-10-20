import React from "react";
import { CardColumnGroup } from "@scm-manager/ui-components";
import { PluginGroup } from "@scm-manager/ui-types";
import PluginEntry from "./PluginEntry";

type Props = {
  group: PluginGroup;
  refresh: () => void;
};

class PluginGroupEntry extends React.Component<Props> {
  render() {
    const { group, refresh } = this.props;
    const entries = group.plugins.map(plugin => {
      return (
        <PluginEntry plugin={plugin} key={plugin.name} refresh={refresh} />
      );
    });
    return <CardColumnGroup name={group.name} elements={entries} />;
  }
}

export default PluginGroupEntry;
