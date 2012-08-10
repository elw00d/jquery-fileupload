/* Russian (UTF-8) initialisation for the jQuery file upload plugin. */
jQuery(function($) {
    $.fileupload.regional['ru'] = {
        clientAbortMessage: 'Загрузка прервана пользователем',
        sizeLimitExceededMessage: 'Превышен допустимый размер файла',
        internalErrorMessage: 'При загрузке произошла ошибка, попробуйте ещё раз',
        filesNotSelectedMessage: 'Выберите файл',
        uploadNotFinishedYetMessage: 'Дождитесь завершения загрузки или отмените её нажатием ESC'
    };
    $.fileupload.setDefaults($.fileupload.regional['ru']);
});