import { FC } from "react";

export type ProtocolLinkRendererProps = {
  protocol: string;
  href: string;
};

export type ProtocolLinkRendererExtension = {
  protocol: string;
  renderer: FC<ProtocolLinkRendererProps>;
};

export type ProtocolLinkRendererExtensionMap = {
  [protocol: string]: FC<ProtocolLinkRendererProps>;
}
