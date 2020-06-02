---
title: Kubernetes
subtitle: Install scm-manager on kubernetes with helm
displayToc: true
---

To install SCM-Manager on Kubernetes we offer a [Helm](https://helm.sh) chart.
The chart is only tested with v3 of helm.

## Quickstart

```bash
helm repo add scm-manager https://packages.scm-manager.org/repository/helm-v2-releases/
helm repo update
helm install scm-manager scm-manager/scm-manager
```

## Configuration

If you want to customize the installation you can use a values files e.g.:

```bash
helm install scm-manager scm-manager/scm-manager --values=custom.yml
```

The following table list the configurable parameters of the SCM-Manager chart and their default values.

## Chart Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| affinity | object | `{}` | Affinity settings |
| fullnameOverride | string | `""` | Override the full resource names |
| image.pullPolicy | string | `"IfNotPresent"` | SCM-Manager image pull policy |
| image.repository | string | `"scmmanager/scm-manager"` |  Name of SCM-Manager image |
| image.tag | string | `"version-off Chart"` | Tag of SCM-Manager image |
| ingress.annotations | object | `{}` | Ingress annotations |
| ingress.enabled | bool | `false` | Enables ingress |
| ingress.hosts | list | `["scm-manager.local"]` | Ingress hosts |
| ingress.path | string | `"/"` | Ingress path |
| ingress.tls | list | `[]` | Ingress TLS configuration |
| nameOverride | string | `""` | Override the resource name prefix |
| nodeSelector | object | `{}` | Node labels for pod assignment |
| persistence.accessMode | string | `"ReadWriteOnce"` | The PVC access mode |
| persistence.enabled | bool | `true` | Enable the use of a PVC for SCM-Manager home |
| persistence.size | string | `"40Gi"` | The size of the PVC |
| resources | object | `{}` | Resources allocation (Requests and Limits) |
| service.port | int | `80` | k8s service port |
| service.type | string | `"LoadBalancer"` | k8s service type |
| tolerations | list | `[]` | Toleration labels for pod assignment |
