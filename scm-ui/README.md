# scm-ui

## VSCode Plugins

* EditorConfig for VS Code
* Flow Language Support
* Prettier - Code formatter
* Project Snippets
* Debugger for Chrome

```bash
code --install-extension EditorConfig.EditorConfig
code --install-extension flowtype.flow-for-vscode
code --install-extension esbenp.prettier-vscode
code --install-extension rebornix.project-snippets

# debugging with chrome browser
code --install-extension msjsdiag.debugger-for-chrome
```

## Install pre-commit hook

```bash
echo "" >> .hg/hgrc
echo "[hooks]" >> .hg/hgrc
echo "pre-commit = cd scm-ui && yarn run pre-commit" >> .hg/hgrc
```
