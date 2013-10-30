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

if (Ext.form.VTypes) {

  Ext.override(Ext.form.VTypes, {
    // sonia.config.js
    pluginurlText: 'Este campo debe ser una URL. Formato: \n\
      "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false"',
    passwordText: '¡Las contraseñas no coinciden!',
    nameTest: 'El nombre no es válido.',
    usernameText: 'El nombre de usuario no es válido.',
    repositoryNameText: 'El nombre del repositorio no es válido.'
  });

}

// sonia.util.js

if (Ext.util.Format) {

  Ext.apply(Ext.util.Format, {
    timeAgoJustNow: 'Ahora',
    timeAgoOneMinuteAgo: 'Hace un minuto',
    timeAgoOneMinuteFromNow: 'Dentro de un minuto',
    timeAgoMinutes: 'Minutos',
    timeAgoOneHourAgo: 'Hace una hora',
    timeAgoOneHourFromNow: 'Dentro de una hora',
    timeAgoHours: 'Horas',
    timeAgoYesterday: 'Ayer',
    timeAgoTomorrow: 'Mañana',
    timeAgoDays: 'Días',
    timeAgoLastWeek: 'La semana pasada',
    timeAgoNextWeek: 'La próxima semana',
    timeAgoWeeks: 'Semanas',
    timeAgoLastMonth: 'El mes pasado',
    timeAgoNextMonth: 'El próximo mes',
    timeAgoMonths: 'Meses',
    timeAgoLastYear: 'El año pasado',
    timeAgoNextYear: 'El próximo año',
    timeAgoYears: 'Años',
    timeAgoLastCentury: 'El siglo pasado',
    timeAgoNextCentury: 'El próximo siglo',
    timeAgoCenturies: 'Siglos'
  });

}

// sonia.login.js

if (Sonia.login.Form) {

  Ext.override(Sonia.login.Form, {
    usernameText: 'Nombre de usuario',
    passwordText: 'Contraseña',
    loginText: 'Iniciar sesión',
    cancelText: 'Cancelar',
    waitTitleText: 'Conectando',
    WaitMsgText: 'Enviando datos...',
    failedMsgText: '¡Error de acceso!',
    failedDescriptionText: 'O el nombre de usuario o la contraseña son incorrectos o usted \n\
                            no tiene suficientes permisos. Por favor inténtelo de nuevo.',
    accountLockedText: 'La cuenta está bloqueada.',
    accountTemporaryLockedText: 'En este momento la cuenta está bloqueada. \n\
                                  Inténtelo más tarde.',
    rememberMeText: 'Recuérdame'
  });

}

if (Sonia.login.Window) {

  Ext.override(Sonia.login.Window, {
    titleText: 'Iniciar sesión'
  });

}

// sonia.rest.js

if (Sonia.rest.JsonStore) {

  Ext.override(Sonia.rest.JsonStore, {
    errorTitleText: 'Error',
    errorMsgText: 'No se pudieron cargar los datos. Estado del servidor: {0}'
  });

}

if (Sonia.rest.Grid) {

  Ext.override(Sonia.rest.Grid, {
    emptyText: 'No se encontraron datos.'
  });

}

if (Sonia.rest.FormPanel) {

  Ext.override(Sonia.rest.FormPanel, {
    okText: 'Aceptar',
    cancelText: 'Cancelar',
    addText: 'Añadir',
    removeText: 'Eliminar'
  });

}

if (Sonia.repository.ChangesetPanel) {

  Ext.apply(Sonia.repository.ChangesetPanel, {
    diffLabel: 'Diferenciar',
    rawDiffLabel: 'Fichero de diferencias'
  });

}

// sonia.repository.js

if (Sonia.repository.DefaultPanel) {

  Ext.apply(Sonia.repository.DefaultPanel, {
    title: 'Repositorio',
    html: 'Añada o seleccione un repositorio'
  });

}

if (Sonia.repository.Grid) {

  Ext.override(Sonia.repository.Grid, {
    colNameText: 'Nombre',
    colTypeText: 'Tipo',
    colContactText: 'Contacto',
    colDescriptionText: 'Descripción',
    colCreationDateText: 'Fecha de creación',
    colUrlText: 'URL',
    colArchiveText: 'Archivo',
    emptyText: 'El repositorio no está configurado',
    formTitleText: 'Repositorio',
    unknownType: 'Desconocido'
  });

}

if (Sonia.repository.FormPanel) {

  Ext.override(Sonia.repository.FormPanel, {
    colGroupPermissionText: 'Es un grupo',
    colNameText: 'Nombre',
    colTypeText: 'Tipo',
    formTitleText: 'Ajustes',
    nameText: 'Nombre',
    typeText: 'Tipo',
    contactText: 'Contacto',
    descriptionText: 'Descripción',
    publicText: 'Público',
    permissionText: 'Permisos',
    errorTitleText: 'Error',
    updateErrorMsgText: 'Error al actualizar el repositorio',
    createErrorMsgText: 'Error el crear el repositorio',
    // help
    nameHelpText: 'El nombre del repositorio. Este nombre es parte de la URL del repositorio.',
    typeHelpText: 'El tipo del repositorio (por ejemplo, Mercurial, Git o Subversion).',
    contactHelpText: 'La dirección de correo electrónico de la persona responsable del repositorio.',
    descriptionHelpText: 'Una breve descripción del repositorio.',
    publicHelpText: 'Un repositorio público puede ser leído por cualquier persona.',
    permissionHelpText: 'Los permisos de gestión para un usuario o un grupo específico.<br />\n\
        Permisos:<br /><b>READ</b> = lectura<br /><b>WRITE</b> = lectura y escritura<br />\n\
        <b>OWNER</b> = lectura, escritura y gestión de propiedades y permisos'
  });

}

if (Sonia.repository.Panel) {

  Ext.override(Sonia.repository.Panel, {
    titleText: 'Repositorio',
    filterText: 'Filtrar: ',
    searchText: 'Buscar: ',
    archiveText: 'Archivar',
    unarchiveText: 'Restaurar',
    archiveTitleText: 'Archivar el repositorio',
    unarchiveTitleText: 'Restaurar el repositorio',
    archiveMsgText: '¿Archivar el repositorio "{0}"?',
    unarchiveMsgText: '¿Restaurar el repositorio "{0}"?',
    errorArchiveMsgText: 'Error al archivar el repositorio.',
    errorUnarchiveMsgText: 'Error al restaurar el repositorio.',
    displayArchivedRepositoriesText: 'Archivo',
    emptyText: 'Añada o seleccione un repositorio',
    removeTitleText: 'Eliminar el repositorio',
    removeMsgText: 'Eliminar el repositorio "{0}"?',
    errorTitleText: 'Error',
    errorMsgText: 'Error al eliminar el repositorio'
  });

}

if (Sonia.repository.PermissionFormPanel) {

  Ext.override(Sonia.repository.PermissionFormPanel, {
    titleText: 'Permisos'
  });

}

if (Sonia.repository.ChangesetViewerPanel) {

  Ext.override(Sonia.repository.ChangesetViewerPanel, {
    // spanish ??
    changesetViewerTitleText: 'Commits {0}' //Confirmaciones
  });

}

if (Sonia.repository.InfoPanel) {

  Ext.override(Sonia.repository.InfoPanel, {
    nameText: 'Nombre: ',
    typeText: 'Tipo: ',
    contactText: 'Contacto: ',
    urlText: 'URL: ',
    // spanish ??
    changesetViewerText: 'Commits', //Confirmaciones
    accessText: 'Acceso:',
    accessReadOnly: 'Acceso de solo lectura',
    accessReadWrite: 'Acceso de lectura y escritura'
  });

}

if (Sonia.repository.ExtendedInfoPanel) {

  Ext.override(Sonia.repository.ExtendedInfoPanel, {
    // spanish ??
    checkoutText: 'Checkout: ', //Obtener
    repositoryBrowserText: 'Código fuente'
  });

}

if (Sonia.repository.RepositoryBrowser) {

  Ext.override(Sonia.repository.RepositoryBrowser, {
    repositoryBrowserTitleText: 'Código fuente {0}',
    emptyText: 'Este directorio está vacio',
    colNameText: 'Nombre',
    colLengthText: 'Longitud',
    colLastModifiedText: 'Última modificación',
    colDescriptionText: 'Descripción'
  });

}

if (Sonia.repository.ChangesetViewerGrid) {

  Ext.override(Sonia.repository.ChangesetViewerGrid, {
    // spanish ??
    emptyText: 'No hay commits disponibles' //confirmaciones
  });

}

if (Sonia.repository.ImportWindow) {

  Ext.override(Sonia.repository.ImportWindow, {
    titleText: 'Importar repositorios',
    okText: 'Aceptar',
    closeText: 'Cerrar'
  });

}

// sonia.config.js

if (Sonia.config.ScmConfigPanel) {

  Ext.override(Sonia.config.ScmConfigPanel, {
    titleText: 'Ajustes generales',
    servnameText: 'Nombre del servidor',
    dateFormatText: 'Formato de la fecha',
    enableForwardingText: 'Habilitar el reenvío (mod_proxy)',
    forwardPortText: 'Puerto para el reenvío',
    pluginRepositoryText: 'Repositorio de plugins',
    allowAnonymousAccessText: 'Permitir el acceso anónimo',
    enableSSLText: 'Activar SSL',
    sslPortText: 'Puerto SSL',
    adminGroupsText: 'Grupos de administradores',
    adminUsersText: 'Usuarios administradores',
    enableProxyText: 'Activar proxy',
    proxyServerText: 'Servidor proxy',
    proxyPortText: 'Puerto para el proxy',
    proxyUserText: 'Usuario para el proxy',
    proxyPasswordText: 'Contraseña para el proxy',
    proxyExcludesText: 'Excepciones para el proxy',
    baseUrlText: 'URL base',
    forceBaseUrlText: 'Forzar a URL base',
    disableGroupingGridText: 'Desactivar la agrupación de repositorios',
    enableRepositoryArchiveText: 'Habilitar el archivado de repositorios',
    submitText: 'Enviando ...',
    loadingText: 'Cargando ...',
    errorTitleText: 'Error',
    errorMsgText: 'No se pudo cargar la configuración.',
    errorSubmitMsgText: 'No se pudo enviar la configuración.',
    loginAttemptLimitText: 'Intentos de inicio de sesión fallidos',
    loginAttemptLimitTimeoutText: 'Tiempo de espera para inicio de sesión',
    // help
    servernameHelpText: 'El nombre de este servidor. Este nombre será parte de la URL del repositorio.',
    dateFormatHelpText: 'Formato de la fecha. Por favor, eche un vistazo a\n\
        <a href="http://momentjs.com/docs/#/displaying/format/" target="_blank">http://momentjs.com/docs/#/displaying/format/</a>.',
    pluginRepositoryHelpText: 'La URL del repositorio de plugins.<br /> Descripción de los {comodines}:\n\
        <br /><b>version</b> = Versión de SCM-Manager<br /><b>os</b> = Sistema Operativo<br /><b>arch</b> = Arquitectura',
    enableForwardingHelpText: 'Habilitar el reenvío de puerto de Apache mod_proxy.',
    forwardPortHelpText: 'El puerto para el reenvío de puerto de Apache mod_proxy.',
    allowAnonymousAccessHelpText: 'Los usuarios anónimos pueden leer los repositorios públicos.',
    enableSSLHelpText: 'Habilitar conexiones seguras a través de HTTPS.',
    sslPortHelpText: 'El puerto SSL.',
    adminGroupsHelpText: 'Lista de grupos con derechos de administrador separados por comas.',
    adminUsersHelpText: 'Lista de usuarios con derechos de administrador separados por comas.',
    loginAttemptLimitHelpText: 'El número máximo de inicios de sesión fallidos. Un valor de -1 desactiva el límite.',
    loginAttemptLimitTimeoutHelpText: 'El tiempo, en segundos, que un usuario será bloqueado temporalmente debido a muchos intentos fallidos de inicio de sesión.',
    enableProxyHelpText: 'Usar la configuración del proxy.',
    proxyServerHelpText: 'El servidor proxy.',
    proxyPortHelpText: 'El puerto para el proxy',
    proxyUserHelpText: 'El nombre de usuario para la autenticación en el servidor proxy.',
    proxyPasswordHelpText: 'La contraseña del usuario para la autenticación en el servidor proxy.',
    proxyExcludesHelpText: 'Una lista separada por comas de los patrones globales de nombre de servidor que serán excluidos de la configuración del proxy',
    baseUrlHelpText: 'La URL completa del servidor, incluyendo el contexto, por ejemplo: http://localhost:8080/scm.',
    forceBaseUrlHelpText: 'Redirige a la URL base si la solicitud proviene de otra URL.',
    disableGroupingGridHelpText: 'Desactiva la agrupación de repositorios. Si se cambia este valor, la página se tiene que volver a cargar.',
    enableRepositoryArchiveHelpText: 'Habilita el archivado de repositorios. Si se cambia este valor, la página se tiene que volver a cargar.'
  });

}

if (Sonia.config.ConfigForm) {

  Ext.override(Sonia.config.ConfigForm, {
    title: 'Configuración',
    saveButtonText: 'Guardar',
    resetButtontext: 'Reestablecer',
    submitText: 'Enviando ...',
    loadingText: 'Cargando ...',
    failedText: 'Se ha producido un error desconocido.'
  });

}

// sonia.user.js

if (Sonia.user.DefaultPanel) {

  Ext.apply(Sonia.user.DefaultPanel, {
    title: 'Usuario',
    html: 'Añada o seleccione un usuario.'
  });

}

if (Sonia.user.Grid) {

  Ext.override(Sonia.user.Grid, {
    titleText: 'Usuario',
    colNameText: 'Nombre',
    colDisplayNameText: 'Mostrar nombre',
    colMailText: 'Correo electrónico',
    colActiveText: 'Activo',
    colAdminText: 'Administrador',
    colCreationDateText: 'Fecha de creación',
    colLastModifiedText: 'Última modificación',
    colTypeText: 'Tipo'
  });

}

if (Sonia.user.FormPanel) {

  Ext.override(Sonia.user.FormPanel, {
    nameText: 'Nombre',
    displayNameText: 'Mostrar nombre',
    mailText: 'Correo electrónico',
    passwordText: 'Contraseña',
    adminText: 'Administrador',
    activeText: 'Activo',
    errorTitleText: 'Error',
    updateErrorMsgText: 'No se ha podido actualizar el usuario',
    createErrorMsgText: 'No se ha podido crear el usuario',
    passwordMinLengthText: 'La contraseña debe tener al menos 6 caracteres',
    // help
    usernameHelpText: 'Nombre único de usuario.',
    displayNameHelpText: 'Muestra el nombre del usuario.',
    mailHelpText: 'Dirección de correo electrónico del usuario.',
    passwordHelpText: 'Contraseña en texto plano del usuario.',
    passwordConfirmHelpText: 'Repita la contraseña para la validación.',
    adminHelpText: 'Un administrador puede crear, editar y borrar repositorios, grupos y usuarios.',
    activeHelpText: 'Activa o desactiva el usuario.'
  });

}

if (Sonia.user.Panel) {

  Ext.override(Sonia.user.Panel, {
    titleText: 'Usuario',
    emptyText: 'Añada o seleccione un usuario',
    removeTitleText: 'Eliminar usuario',
    removeMsgText: '¿Eliminar el usuario "{0}"?',
    showOnlyActiveText: 'Mostrar solo activos: ',
    errorTitleText: 'Error',
    errorMsgText: 'No se ha podido eliminar el usuario'
  });

}


// sonia.group.js

if (Sonia.group.DefaultPanel) {

  Ext.apply(Sonia.group.DefaultPanel, {
    title: 'Grupo',
    html: 'Añada o seleccione un grupo'
  });

}

if (Sonia.group.Grid) {

  Ext.override(Sonia.group.Grid, {
    colNameText: 'Nombre',
    colDescriptionText: 'Descripción',
    colMembersText: 'Miembros',
    colCreationDateText: 'Fecha de creación',
    colTypeText: 'Tipo',
    emptyGroupStoreText: 'El grupo no está configurado.',
    groupFormTitleText: 'Grupo'
  });

}

if (Sonia.group.FormPanel) {

  Ext.override(Sonia.group.FormPanel, {
    colMemberText: 'Miembro',
    titleText: 'Ajustes',
    nameText: 'Nombre',
    descriptionText: 'Descripción',
    membersText: 'Miembros',
    errorTitleText: 'Error',
    updateErrorMsgText: 'No se pudo actualizar el grupo',
    createErrorMsgText: 'No se pudo crear el grupo',
    // help
    nameHelpText: 'Nombre único del grupo.',
    descriptionHelpText: 'Una breve descripción del grupo.',
    membersHelpText: 'Nombres de usuario de los miembros del grupo.'
  });

}

if (Sonia.group.Panel) {

  Ext.override(Sonia.group.Panel, {
    titleText: 'Grupo',
    emptyText: 'Añada o seleccione un grupo',
    removeTitleText: 'Eliminar grupo',
    removeMsgText: '¿Eliminar el grupo "{0}"?',
    errorTitleText: 'Error',
    errorMsgText: 'No se pudo eliminar el grupo.'
  });

}

// sonia.action.js

if (Sonia.action.ChangePasswordWindow) {

  Ext.override(Sonia.action.ChangePasswordWindow, {
    titleText: 'Cambiar contraseña',
    oldPasswordText: 'Clave antigua', // Better 'Contraseña antigua' change dialog size
    newPasswordText: 'Clave nueva', // Better 'Contraseña nueva' change dialog size
    confirmPasswordText: 'Confirma clave', // Better 'Confirma clave' change dialog size
    okText: 'Aceptar',
    cancelText: 'Cancelar',
    connectingText: 'Conectando',
    failedText: '¡No se pudo cambiar la contraseña!',
    waitMsgText: 'Enviando datos ...'
  });

}

if (Sonia.action.ExceptionWindow) {

  // ??
  Ext.override(Sonia.action.ExceptionWindow, {
    okText: 'Aceptar',
    detailsText: 'Detalles',
    exceptionText: 'Excepción'
  });

}

// sonia.plugin.js

if (Sonia.plugin.Center) {

  Ext.override(Sonia.plugin.Center, {
    waitTitleText: 'Por favor espere',
    errorTitleText: 'Error',
    restartText: 'Reinicie el servidor para activar el pugin.',
    installWaitMsgText: 'Instalando el plugin.',
    installSuccessText: 'Instalación correcta',
    installFailedText: 'La instalación falló',
    uninstallWaitMsgText: 'Desinstalando el plugin.',
    uninstallSuccessText: 'Desinstalación correcta',
    uninstallFailedText: 'La desinstalación falló',
    updateWaitMsgText: 'Actualizando el plugin.',
    updateSuccessText: 'Actualización correcta',
    updateFailedText: 'La actualización falló'

        /*
         Better:
         
         installSuccessText: 'El plugin se instaló correctamente',
         installFailedText: 'La instalación del plugin ha fallado',
         
         uninstallSuccessText: 'El plugin se desinstaló correctamente',
         uninstallFailedText: 'La desinstalación del plugin ha fallado',
         
         updateSuccessText: 'El plugin se actualizó correctamente',
         updateFailedText: 'La actualización del plugin ha fallado'
         
         But needed change dialog size
         */
  });

}

if (Sonia.plugin.Grid) {

  Ext.override(Sonia.plugin.Grid, {
    colNameText: 'Nombre',
    colAuthorText: 'Autor',
    colDescriptionText: 'Descripción',
    colVersionText: 'Versión',
    colActionText: 'Acción',
    colUrlText: 'URL',
    colCategoryText: 'Categoría',
    emptyText: 'No hay plugins disponibles.',
    btnReload: 'Actualizar',
    btnInstallPackage: 'Instalar paquete',
    uploadWindowTitle: 'Cargar paquete de plugins'
  });

}

if (Sonia.plugin.UploadForm) {

  Ext.override(Sonia.plugin.UploadForm, {
    emptyText: 'Seleccione un paquete de plugins',
    uploadFieldLabel: 'Paquete',
    waitMsg: 'Cargando el paquete ...',
    btnUpload: 'Cargar',
    btnReset: 'Limpiar'
  });

}

// sonia.scm.js

if (Sonia.scm.Main) {

  Ext.override(Sonia.scm.Main, {
    tabRepositoriesText: 'Repositorios',
    navChangePasswordText: 'Cambiar contraseña',
    sectionMainText: 'Principal',
    sectionSecurityText: 'Seguridad',
    navRepositoriesText: 'Repositorios',
    sectionConfigText: 'Configuración',
    navGeneralConfigText: 'General',
    tabGeneralConfigText: 'Configuración general',
    navRepositoryTypesText: 'Repositorios',
    navImportRepositoriesText: 'Importar repositorios',
    tabRepositoryTypesText: 'Configuración de repositorios',
    navPluginsText: 'Plugins',
    tabPluginsText: 'Configuración de plugins',
    navUsersText: 'Usuarios',
    tabUsersText: 'Usuarios',
    navGroupsText: 'Grupos',
    tabGroupsText: 'Grupos',
    sectionLoginText: 'Iniciar sesión',
    navLoginText: 'Iniciar sesión',
    sectionLogoutText: 'Cerrar sesión',
    navLogoutText: 'Cerrar sesión',
    logoutFailedText: '¡Ha ocurrido un error al salir!',
    errorTitle: 'Error',
    errorMessage: 'Se ha producido un error desconocido.',
    errorSessionExpiredTitle: 'Sesión expirada',
    errorSessionExpiredMessage: 'Su sesión ha expirado. Por favor, inicie sesión de nuevo.',
    errorNoPermissionsTitle: 'Sin permiso',
    errorNoPermissionsMessage: 'Usted no tiene permiso para realizar esta acción.',
    errorNotFoundTitle: 'No encontrado',
    errorNotFoundMessage: 'El recurso no se pudo encontrar.',
    loggedInTextTemplate: 'conectado como <a id="scm-userinfo-tip">{state.user.name}</a> - ',
    userInfoMailText: 'Correo electrónico',
    userInfoGroupsText: 'Grupos'
  });

}

if (Sonia.panel.SyntaxHighlighterPanel) {

  Ext.override(Sonia.panel.SyntaxHighlighterPanel, {
    loadErrorTitleText: 'Error',
    loadErrorMsgText: 'No se pudo cargar el fichero'
  });

}


if (Sonia.rest.Panel) {

  Ext.override(Sonia.rest.Panel, {
    addText: 'Añadir',
    removeText: 'Eliminar',
    reloadText: 'Actualizar'
  });

}

// sonia.security.permissionspanel.js
if (Sonia.security.PermissionsPanel) {

  Ext.override(Sonia.security.PermissionsPanel, {
    addText: 'Añadir',
    removeText: 'Eliminar',
    reloadText: 'Actualizar',
    titleText: 'Permisos'
  });

}