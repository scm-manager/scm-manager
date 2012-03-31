/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */

if (Ext.form.VTypes){

  Ext.override(Ext.form.VTypes, {

    // sonia.config.js
    pluginurlText: 'Dieses Feld sollte eine URL enthalten. Format: \n\
      "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false"',

    passwordText: 'Die Passwörter stimmen nicht überein!',
    nameTest: 'Der Name ist invalid.',
    usernameText: 'Der Benutzername ist invalid.'
  });

}

// sonia.util.js

if ( Ext.util.Format ){
  
  Ext.apply(Ext.util.Format, {
    timeAgoJustNow: 'Jetzt',
    timeAgoOneMinuteAgo: 'Vor 1 Minute',
    timeAgoOneMinuteFromNow: 'In 1 minute',
    timeAgoMinutes: 'Minuten',
    timeAgoOneHourAgo: 'Vor 1 Stunde',
    timeAgoOneHourFromNow: 'In 1 Stunde',
    timeAgoHours: 'Stunden',
    timeAgoYesterday: 'Gestern',
    timeAgoTomorrow: 'Morgen',
    timeAgoDays: 'Tage',
    timeAgoLastWeek: 'Letzte Woche',
    timeAgoNextWeek: 'Nächste Woche',
    timeAgoWeeks: 'Wochen',
    timeAgoLastMonth: 'Letzten Monat',
    timeAgoNextMonth: 'Nächsten Monat',
    timeAgoMonths: 'Monate',
    timeAgoLastYear: 'Letztes Jahr',
    timeAgoNextYear: 'Nächstes Jahr',
    timeAgoYears: 'Jahre',
    timeAgoLastCentury: 'Letztes Jahrhundert',
    timeAgoNextCentury: 'Nächstes Jahrhundert',
    timeAgoCenturies: 'Jahrhunderte'
  });
  
}

// sonia.login.js

if (Sonia.login.Form){

  Ext.override(Sonia.login.Form, {
    usernameText: 'Benutzername',
    passwordText: 'Passwort',
    loginText: 'Anmelden',
    cancelText: 'Abbrechen',
    waitTitleText: 'Verbinden',
    WaitMsgText: 'Übertrage Daten...',
    failedMsgText: 'Anmeldung fehlgeschlagen!'
  });

}

if (Sonia.login.Window){
  
  Ext.override(Sonia.login.Window, {
    titleText: 'Anmeldung'
  });

}

// sonia.rest.js

if ( Sonia.rest.JsonStore ){

  Ext.override(Sonia.rest.JsonStore, {
    errorTitleText: 'Fehler',
    errorMsgText: 'Es konnten keine Daten geladen werden. Server-Status: {0}'
  });

}

if ( Sonia.rest.Grid ){

  Ext.override(Sonia.rest.Grid, {
    emptyText: 'Es konnten keine Daten gefunden werden.'
  });

}

if ( Sonia.rest.FormPanel ){

  Ext.override(Sonia.rest.FormPanel, {
    okText: 'Ok',
    cancelText: 'Abbrechen',
    addText: 'Hinzufügen',
    removeText: 'Entfernen'
  });

}

// sonia.repository.js

if (Sonia.repository.DefaultPanel){
  
  Ext.apply(Sonia.repository.DefaultPanel, {
    title: 'Repository',
    html: 'Es wurde kein Repository selektiert'
  });
  
}

if ( Sonia.repository.Grid ){

  Ext.override(Sonia.repository.Grid, {
    colNameText: 'Name',
    colTypeText: 'Typ',
    colContactText: 'Kontakt',
    colDescriptionText: 'Beschreibung',
    colCreationDateText: 'Erstellungsdatum',
    colUrlText: 'Url',
    emptyText: 'Es wurde kein Repository konfiguriert',
    formTitleText: 'Repository',
    unknownType: 'Unbekannt'
  });

}

if (Sonia.repository.FormPanel){

  Ext.override(Sonia.repository.FormPanel, {
    colGroupPermissionText: 'Grouppen Berechtigung',
    colNameText: 'Name',
    colTypeText: 'Typ',
    formTitleText: 'Einstellungen',
    nameText: 'Name',
    typeText: 'Typ',
    contactText: 'Kontakt',
    descriptionText: 'Beschreibung',
    publicText: 'Öffentlich',
    permissionText: 'Berechtigung',
    errorTitleText: 'Fehler',
    updateErrorMsgText: 'Repository update fehlgeschlagen',
    createErrorMsgText: 'Repository erstellen fehlgeschlagen',

    // help
    nameHelpText: 'Name des Repositories. Dieser Name wird Teil der Repository-URL.',
    typeHelpText: 'Typ des Repositories (z.B.: Mercurial, Git oder Subversion).',
    contactHelpText: 'Die E-Mail Adresse einer für das Repository verantwortlichen Person.',
    descriptionHelpText: 'Eine kurze Beschreibung des Repositories.',
    publicHelpText: 'Ein öffentliches Repository kann von jeder Person gelesen werden.',
    permissionHelpText: 'Rechteverwaltung für bestimmte Nutzer oder Gruppen<br />\n\
        Rechte:<br /><b>READ</b> = nur lesen<br /><b>WRITE</b> = lesen und schreiben<br />\n\
        <b>OWNER</b> = lesen, schreiben und Rechteverwaltung.'
  });

}

if (Sonia.repository.Panel){

  Ext.override(Sonia.repository.Panel, {
    titleText: 'Repository',
    emptyText: 'Es wurde kein Repository selektiert',
    removeTitleText: 'Repository entfernen',
    removeMsgText: 'Repository entfernen "{0}"?',
    errorTitleText: 'Fehler',
    errorMsgText: 'Repository entfernen fehlgeschlagen'
  });

}

if (Sonia.repository.PermissionFormPanel){
  
  Ext.override(Sonia.repository.PermissionFormPanel, {
    titleText: 'Berechtigungen'
  });
  
}

if (Sonia.repository.ChangesetViewerPanel){
  
  Ext.override(Sonia.repository.ChangesetViewerPanel,{
    // german ??
    changesetViewerTitleText: 'Commits {0}'
  });
  
}

if (Sonia.repository.InfoPanel){

  Ext.override(Sonia.repository.InfoPanel, {
    nameText: 'Name: ',
    typeText: 'Typ: ',
    contactText: 'Kontakt: ',
    urlText: 'Url: ',
    // german ??
    changesetViewerText: 'Commits'
  });

}

if (Sonia.repository.ExtendedInfoPanel){
  
  Ext.override(Sonia.repository.ExtendedInfoPanel, {
    // german ??
    checkoutText: 'Checkout: ',
    repositoryBrowserText: 'Source'
  });
  
}

if (Sonia.repository.RepositoryBrowser){
  
  Ext.override(Sonia.repository.RepositoryBrowser, {
    // german ??
    repositoryBrowserTitleText: 'Source {0}',
    emptyText: 'In diesem Verzeichnis befinden sich keine Dateien'
  });
  
}

if (Sonia.repository.ChangesetViewerGrid){
  
  Ext.override(Sonia.repository.ChangesetViewerGrid, {
    emptyText: 'Es konnten keine Commits gefunden werden'
  });
  
}

// sonia.config.js

if (Sonia.config.ScmConfigPanel){

  Ext.override(Sonia.config.ScmConfigPanel,{
    titleText: 'Allgemeine Einstellung',
    servnameText: 'Servername',
    dateFormatText: 'Datumsformat',
    enableForwardingText: 'Forwarding (mod_proxy) aktivieren',
    forwardPortText: 'Forward Port',
    pluginRepositoryText: 'Plugin-Repository',
    allowAnonymousAccessText: 'Anonymen Zugriff erlauben',
    enableSSLText: 'SSL Aktivieren',
    sslPortText: 'SSL Port',
    adminGroupsText: 'Admin Gruppen',
    adminUsersText: 'Admin Benutzer',
    
    enableProxyText: 'Proxy aktivieren',
    proxyServerText: 'Proxy Server',
    proxyPortText: 'Proxy Port',
    proxyUserText: 'Proxy User',
    proxyPasswordText: 'Proxy Passwort',
    baseUrlText: 'Basis-URL',
    forceBaseUrlText: 'Basis-URL forcieren',
    
    submitText: 'Senden ...',
    loadingText: 'Laden ...',
    errorTitleText: 'Fehler',
    errorMsgText: 'Die Konfiguration konnte nicht geladen werden.',
    errorSubmitMsgText: 'Die Konfiguration konnte nicht gespeichert werden.',

    // help
    servernameHelpText: 'Der Name dieses Servers. Dieser Name wird Teil der Repository-URL.',
    // TODO
    dateFormatHelpText: 'JavaScript Datumsformat.',
    pluginRepositoryHelpText: 'Die URL des Plugin-Repositories.<br />Beschreibung der {Platzhalter}:\n\
      <br /><b>version</b> = SCM-Manager Version<br /><b>os</b> = Betriebssystem<br /><b>arch</b> = Architektur',
    enableForwardingHelpText: 'Apache mod_proxy Port-Forwarding aktivieren.',
    forwardPortHelpText: 'Der Port für das mod_proxy Port-Forwarding.',
    allowAnonymousAccessHelpText: 'Anonyme Benutzer können öffentlich Repositories lesen.',
    enableSSLHelpText: 'Aktiviere sichere Verbindungen über HTTPS.',
    sslPortHelpText: 'Der SSL-Port.',
    adminGroupsHelpText: 'Komma getrennte Liste von Gruppen mit Administrationsrechten.',
    adminUsersHelpText: 'Komma getrennte Liste von Benutzern mit Administrationsrechten.',
    
    enableProxyHelpText: 'Proxy-Einstellungen verwenden.',
    proxyServerHelpText: 'Der Proxy-Server.',
    proxyPortHelpText: 'Der Proxy-Port',
    proxyUserHelpText: 'Der Benutzername für die Authentifizierung am Proxy-Server.',
    proxyPasswordHelpText: 'Das Passwort für die Authentifizierung am Proxy-Server.',
    baseUrlHelpText: 'Die vollständige URL des Server, inclusive Context-Pfad z.B.: http://localhost:8080/scm.',
    forceBaseUrlHelpText: 'Leitet alle Zugriffe die nicht von der Basis-URL kommen auf die Basis-URL um.'
  });

}

if (Sonia.config.ConfigForm){

  Ext.override(Sonia.config.ConfigForm, {
    title: 'Konfiguration',
    saveButtonText: 'Speichern',
    resetButtontext: 'Zurücksetzen',

    submitText: 'Senden ...',
    loadingText: 'Laden ...',
    failedText: 'Es ist ein unbekannter Fehler aufgetreten.'
  });

}

// sonia.user.js

if (Sonia.user.DefaultPanel){
  
  Ext.apply(Sonia.user.DefaultPanel, {
    title: 'Benutzer',
    html: 'Es wurde kein Benutzer selektiert.'
  });
  
}

if (Sonia.user.Grid){

  Ext.override(Sonia.user.Grid, {
    titleText: 'Benutzer',
    colNameText: 'Name',
    colDisplayNameText: 'Anzeigename',
    colMailText: 'E-Mail',
    colAdminText: 'Admin',
    colCreationDateText: 'Erstellungsdatum',
    colLastModifiedText: 'Letzte Änderung',
    colTypeText: 'Typ'
  });

}

if (Sonia.user.FormPanel){

  Ext.override(Sonia.user.FormPanel, {
    nameText: 'Name',
    displayNameText: 'Anzeigename',
    mailText: 'E-Mail',
    passwordText: 'Passwort',
    adminText: 'Administrator',
    errorTitleText: 'Fehler',
    updateErrorMsgText: 'Benutzer Aktualisierung fehlgeschlagen',
    createErrorMsgText: 'Benutzer Erstellung fehlgeschlagen',
    passwordMinLengthText: 'Das Passwort muss mindestens 6 Zeichen lang sein',

    // help
    usernameHelpText: 'Eindeutiger Name des Benutzers.',
    displayNameHelpText: 'Anzeigename des Benutzers.',
    mailHelpText: 'E-Mail Adresse des Benutzers.',
    passwordHelpText: 'Passwort des Benutzers.',
    passwordConfirmHelpText: 'Passwortwiederholung zur Kontrolle.',
    adminHelpText: 'Ein Administrator kann Repositories, Gruppen und Benutzer erstellen, bearbeiten und löschen.'
  });

}

if (Sonia.user.Panel){

  Ext.override(Sonia.user.Panel, {
    titleText: 'Benutzer',
    emptyText: 'Es wurde kein Benutzer selektiert',
    removeTitleText: 'Benutzer entfernen',
    removeMsgText: 'Benutzer "{0}" entfernen?',
    errorTitleText: 'Fehler',
    errorMsgText: 'Entfernen des Benutzers fehlgeschlagen'
  });

}


// sonia.group.js

if (Sonia.group.DefaultPanel){
  
  Ext.apply(Sonia.group.DefaultPanel,{
    title: 'Gruppe',
    html: 'Es wurde keine Gruppe selektiert'
  });
  
}

if (Sonia.group.Grid){

  Ext.override(Sonia.group.Grid,{
    colNameText: 'Name',
    colDescriptionText: 'Beschreibung',
    colMembersText: 'Mitglieder',
    colCreationDateText: 'Erstellungsdatum',
    colTypeText: 'Typ',
    emptyGroupStoreText: 'Es wurde keine Gruppe konfiguriert.',
    groupFormTitleText: 'Gruppe'
  });

}

if (Sonia.group.FormPanel){

  Ext.override(Sonia.group.FormPanel, {
    colMemberText: 'Mitglied',
    titleText: 'Einstellungen',
    nameText: 'Name',
    descriptionText: 'Beschreibung',
    membersText: 'Mitglieder',
    errorTitleText: 'Fehler',
    updateErrorMsgText: 'Gruppen Aktualisierung fehlgeschlagen',
    createErrorMsgText: 'Gruppen Erstellung fehlgeschlagen',

    // help
    nameHelpText: 'Eindeutiger Name der Gruppe.',
    descriptionHelpText: 'Eine kurze Beschreibung der Gruppe.',
    membersHelpText: 'Die Benutzernamen der Gruppenmitglieder.'
  });

}

if (Sonia.group.Panel){

  Ext.override(Sonia.group.Panel, {
    titleText: 'Gruppe',
    emptyText: 'Es wurde keine Gruppe selektiert',
    removeTitleText: 'Gruppe entfernen',
    removeMsgText: 'Gruppe "{0}" entfernen?',
    errorTitleText: 'Fehler',
    errorMsgText: 'Entfernen der Gruppe fehlgeschlagen.'
  });

}

// sonia.action.js

if (Sonia.action.ChangePasswordWindow){

  Ext.override(Sonia.action.ChangePasswordWindow, {
    titleText: 'Passwort ändern',
    oldPasswordText: 'Altes Passwort',
    newPasswordText: 'Neues Password',
    confirmPasswordText: 'Passwort bestätigen',
    okText: 'Ok',
    cancelText: 'Abbrechen',
    connectingText: 'Verbinden',
    failedText: 'Passwort Änderung fehlgeschlagen!',
    waitMsgText: 'Daten übertragen...'
  });

}

// sonia.plugin.js

if (Sonia.plugin.Center){

  Ext.override(Sonia.plugin.Center, {
    waitTitleText: 'Bitte warten',
    errorTitleText: 'Fehler',
    restartText: 'Der ApplicationServer muss neugestartet werden um das Plugin zu aktivieren.',

    installWaitMsgText: 'Plugin wird installiert.',
    installSuccessText: 'Plugin wurde erfolgreich installiert',
    installFailedText: 'Plugin installieren fehlgeschlagen',

    uninstallWaitMsgText: 'Plugin wird deinstalliert.',
    uninstallSuccessText: 'Plugin wurde erfolgreich deinstalliert',
    uninstallFailedText: 'Plugin deinstallieren fehlgeschlagen',

    updateWaitMsgText: 'Plugin wird aktualisiert.',
    updateSuccessText: 'Plugin wurde erfolgreich aktualisiert',
    updateFailedText: 'Plugin aktualisieren fehlgeschlagen'
  });

}

if (Sonia.plugin.Grid){

  Ext.override(Sonia.plugin.Grid, {
    colNameText: 'Name',
    colAuthorText: 'Autor',
    colDescriptionText: 'Beschreibung',
    colVersionText: 'Version',
    colActionText: 'Aktion',
    colUrlText: 'Url',
    emptyText: 'Es konnte kein Plugin gefunden werden.'
  });

}

// sonia.scm.js

if (Sonia.scm.Main){

  Ext.override(Sonia.scm.Main, {
    tabRepositoriesText: 'Repositories',
    navChangePasswordText: 'Passwort ändern',
    sectionMainText: 'Repositories',
    sectionSecurityText: 'Sicherheit',
    navRepositoriesText: 'Repositories',
    sectionConfigText: 'Konfiguration',
    navGeneralConfigText: 'Allgemein',
    tabGeneralConfigText: 'SCM Konfiguration',

    navRepositoryTypesText: 'Repository Konfiguration',
    tabRepositoryTypesText: 'Repository Konfiguration',
    navPluginsText: 'Plugins',
    tabPluginsText: 'Plugins',
    navUsersText: 'Benutzer',
    tabUsersText: 'Benutzer',
    navGroupsText: 'Gruppen',
    tabGroupsText: 'Gruppen',

    sectionLoginText: 'Anmelden',
    navLoginText: 'Anmelden',

    sectionLogoutText: 'Abmelden',
    navLogoutText: 'Abmelden',

    logoutFailedText: 'Abmeldung Fehlgeschlagen!',
    
    errorTitle: 'Fehler',
    errorMessage: 'Es ist ein unbekannter Fehler aufgetreten.',
    
    errorSessionExpiredTitle: 'Session abgelaufen',
    errorSessionExpiredMessage: 'Ihre Session ist abgelaufen. Bitte melden sie sich neu an.'
  });

}

if (Sonia.panel.SyntaxHighlighterPanel){
  
  Ext.override(Sonia.panel.SyntaxHighlighterPanel, {
    loadErrorTitleText: 'Fehler',
    loadErrorMsgText: 'Die Datei konnte nicht geladen werden'
  });
  
}


if (Sonia.rest.Panel){
  
  Ext.override(Sonia.rest.Panel, {
    
    addText: 'Hinzufügen',
    removeText: 'Entfernen',
    reloadText: 'Aktualisieren'
    
  });
  
}