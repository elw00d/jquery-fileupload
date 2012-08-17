/* Russian (UTF-8) initialisation for the jQuery file upload plugin. */
jQuery(function($) {
    if ($.fileupload.regional['ru'] == undefined || $.fileupload.regional['ru'] == null)
        $.fileupload.regional['ru'] = { };
    $.extend($.fileupload.regional['ru'], {
                errorMessages: {
                    client_abort: 'Загрузка прервана пользователем',
                    size_limit_exceeded: 'Превышен допустимый размер файла',
                    internal_error: 'При загрузке произошла ошибка, попробуйте ещё раз'
                },
        filesNotSelectedMessage: 'Выберите файл',
        uploadNotFinishedYetMessage: 'Дождитесь завершения загрузки или отмените её нажатием ESC'
    });
    $.fileupload.setDefaults($.fileupload.regional['ru']);
});