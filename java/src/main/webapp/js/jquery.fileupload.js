/*!
 * jQuery fileupload - files upload plugin.
 *
 * (c) elwood
 */
(function($) {
    function FileUpload() {
        // available regional settings, indexed by language code
        this.regional = [];
        // default regional settings
        this.regional[''] = {
            errorMessages: {
                client_abort: 'User has cancelled the upload',
                size_limit_exceeded: 'Size limit exceeded',
                internal_error: 'Internal server error occured'
            },
            filesNotSelectedMessage: 'Not all files are selected',
            uploadNotFinishedYetMessage: 'Wait until upload finishes or cancel the upload using ESC'
        };
        // default settings
        this._defaults = {
            actionUrl: '#',
            uploadProgressUrl: '#',
            uploaderId: '',
            interval: 1500, // progress check interval in ms
            onValidate: null, // validation handler that will be called instead default if specified
            onStarted: null,
            onFinished: null,
            onProgress: null,
            onError: null,
            invokeDefaults: true // if false there are no progress bar displayed
        };
        $.extend(this._defaults, this.regional['']);

        /* Override the default settings for all instances of the control.
           @param  settings  object - the new settings to use as defaults (anonymous object)
           @return the manager object */
        this.setDefaults = function(options) {
            $.extend(this._defaults, options || {});
		    return this;
        };

        this._getSettings = function(uploaderId) {
            return $.fileupload._getFormObject(uploaderId).data('settings');
        };

        this._getFormObject = function(uploaderId) {
            return $('form').filter('[target=fileupload_frame_' + uploaderId + ']');
        };

        // called from iframe js (when the request is totally processed)
        this._uploadFinished = function(uploaderId, timestamp, hasErrors, errorMessage) {
            var formObject = $.fileupload._getFormObject(uploaderId);
            //
            var storedTimestamp = formObject.data('timestamp');
            if (storedTimestamp != timestamp) {
                console.log('Warn: timestamp of upload finished handler differs from timestamp of actual upload.');
                return;
            }
            //
            var settings = formObject.data('settings');
            var storedProgress = formObject.data('progress');
            var executeCallbacks = false;
            //
            if (storedProgress != null) {
                if (storedProgress.status == 'uploaded' && hasErrors || storedProgress.status == 'error' && !hasErrors) {
                    console.log("Warn: stored progress status conflicts with status received from target iframe.");
                    if (hasErrors) {
                        console.log("Error: " + errorMessage);
                    }
                }
                if (storedProgress.status == 'idle' || storedProgress.status == 'progress') {
                    storedProgress.status = hasErrors ? 'error' : 'uploaded';
                    if (hasErrors) {
                        storedProgress.errorMessage = errorMessage;
                    }
                    executeCallbacks = true;
                }
            } else {
                // if we are here, the request about progress has failed
                // we should finish the upload process
                var progress = {
                    bytesReaded: 0,
                    bytesTotal: 0,
                    status: hasErrors ? 'error' : 'uploaded',
                    errorMessage: errorMessage
                };
                formObject.data('progress', progress);
                executeCallbacks = true;
            }
            //
            if (executeCallbacks) {
                if (settings.invokeDefaults) {
                    $.fileupload._finishedDefault(uploaderId, hasErrors, errorMessage);
                }
                //
                if (settings.onFinished != null) {
                    settings.onFinished(uploaderId, hasErrors, errorMessage);
                }
            }
        };

        // called every N ms during upload
        this._refreshProgress = function(uploaderId) {
            var formObject = $.fileupload._getFormObject(uploaderId);
            var settings = formObject.data('settings');
            var storedProgress = formObject.data('progress');
            if (storedProgress != null && (storedProgress.status == 'uploaded' || storedProgress.status == 'error')) {
                // do nothing if upload has been finished already
                return;
            }
            $.ajax(settings.uploadProgressUrl.replace('{uploaderId}', uploaderId), {
                        dataType: 'json',
                        success: function(progress) {
                            var storedProgress = formObject.data('progress');
                            console.assert(storedProgress != null);
                            if (storedProgress.status == 'uploaded' || storedProgress.status == 'error') {
                                return;
                            }
                            formObject.data('progress', progress);
                            //
                            if (progress.status == 'idle' || progress.status == 'progress') {
                                window.setTimeout(function() {
                                    $.fileupload._refreshProgress(uploaderId);
                                }, settings.interval);
                            }
                            //
                            if (progress.status == 'progress') {
                                if (settings.invokeDefaults) {
                                    $.fileupload._progressDefault(uploaderId, progress);
                                }
                                if (settings.onProgress != null) {
                                    settings.onProgress(uploaderId, progress);
                                }
                            } else if (progress.status == 'uploaded' || progress.status == 'error') {
                                if (settings.invokeDefaults) {
                                    $.fileupload._finishedDefault(uploaderId, progress.status == "error", progress.errorMessage);
                                }
                                //
                                if (settings.onFinished != null) {
                                    settings.onFinished(uploaderId, progress.status == "error", progress.errorMessage);
                                }
                            }
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            console.error(jqXHR, textStatus, errorThrown);
                            // let user start a new upload session
                            // and don't deny the upload finished handler to work (if it will be called)
                            formObject.data('progress', null);
                        }
                    });
        };

        // default validation handler before form submission
        this._validateDefault = function(uploaderId) {
            var formObject = $.fileupload._getFormObject(uploaderId);
            var noFile = false;
            formObject.find('[type=file]').each(function(index, item) {
                if (!item.value && !noFile) {
                    var settings = formObject.data('settings');
                    if (settings.onError != null) {
                        settings.onError(uploaderId, settings.filesNotSelectedMessage);
                    } else {
                        $.fileupload._onErrorDefault(uploaderId, settings.filesNotSelectedMessage);
                    }
                    noFile = true;
                }
            });
            return !noFile;
        };

        // default started event handler - called if invokeDefaults is true
        this._startedDefault = function(uploaderId) {
            var formObject = $.fileupload._getFormObject(uploaderId);
            if (!formObject.next().next().is('div') || formObject.next().next()[0].className != 'fileuploadProgress') {
                formObject.next().after('<div class="fileuploadProgress" style="margin-top: 5px; width: 350px; height: 18px; border: 1px inset; background: #eee; display: none;"><div style="width: 0; height: 18px; background: #9ACB34;"></div></div>');
            }
            formObject.next().next().find('div')[0].style.background = '#9ACB34';
            formObject.next().next().find('div')[0].style.width = '0px';
            formObject.next().next()[0].style.display = 'block';
        };

        // default progress changed handler - called if invokeDefaults is true
        this._progressDefault = function(uploaderId, progress) {
            var formObject = $.fileupload._getFormObject(uploaderId);
            var settings = formObject.data('settings');
            if (progress.status == 'progress') {
                var progressPercent = Math.ceil((progress.bytesReaded / progress.bytesTotal) * 100);
                formObject.next().next().find('div')[0].style.width = parseInt(progressPercent * 3.5) + 'px';
            }
        };

        // default finished event handler - called if invokeDefaults is true
        this._finishedDefault = function(uploaderId, hasErrors, errorMessage) {
            var formObject = $.fileupload._getFormObject(uploaderId);
            if (hasErrors) {
                formObject.next().next().find('div')[0].style.width = parseInt(100 * 3.5) + 'px';
                formObject.next().next().find('div')[0].style.background = 'red';
                if (formObject.data('settings').onError != null) {
                    formObject.data('settings').onError(uploaderId, errorMessage);
                } else {
                    $.fileupload._onErrorDefault(uploaderId, errorMessage);
                }
                formObject.next().next()[0].style.display = 'none';
            } else {
                formObject.next().next().find('div')[0].style.width = parseInt(100 * 3.5) + 'px';
                window.setTimeout(function() {
                    formObject.next().next()[0].style.display = 'none';
                }, 1000);
            }
        };

        this._onErrorDefault = function(uploaderId, errorMessage) {
            var formObject = $.fileupload._getFormObject(uploaderId);
            alert($.fileupload._translateErrorMessage(errorMessage, formObject.data('settings')));
        };

        this._translateErrorMessage = function(errorMessage, settings) {
            if (settings.errorMessages[errorMessage] != undefined && settings.errorMessages[errorMessage] != null) {
                return settings.errorMessages[errorMessage];
            }
            // it is custom error message
            return errorMessage;
        };
    }

    $.fn.fileupload = function(options) {

        var settings = $.extend({}, $.fileupload._defaults, options);

        if (!this.next().is('iframe') || this.next().attr('name') != 'fileupload_frame_' + settings.uploaderId) {
            this.after('<iframe style="display: none;" name="fileupload_frame_' + settings.uploaderId + '"></iframe>');
        }
        this.attr('target', 'fileupload_frame_' + settings.uploaderId);

        this.submit(function(e) {
            var formObject = $(e.target);
            //
            if (settings.onValidate != null) {
                if (!settings.onValidate(settings.uploaderId)) {
                    e.preventDefault();
                    return;
                }
            } else {
                if (!$.fileupload._validateDefault(settings.uploaderId)) {
                    e.preventDefault();
                    return;
                }
            }
            //
            var storedProgress = formObject.data('progress');
            if (storedProgress != null && storedProgress != undefined &&
                    storedProgress.status != 'uploaded' && storedProgress.status != 'error') {
                if (settings.onError != null) {
                    settings.onError(settings.uploaderId, settings.uploadNotFinishedYetMessage);
                } else {
                    $.fileupload._onErrorDefault(settings.uploaderId, settings.uploadNotFinishedYetMessage);
                }
                e.preventDefault();
                return;
            }
            window.setTimeout(function() {
                $.fileupload._refreshProgress(settings.uploaderId)
            }, settings.interval);
            //
            var timestamp = new Date().getTime();
            formObject.data('timestamp', timestamp);
            var realActionUrl = settings.actionUrl.replace('{uploaderId}', settings.uploaderId).replace('{timestamp}', timestamp);
            formObject.attr('action', realActionUrl);
            //
            formObject.data('progress', {
                        bytesReaded: 0,
                        bytesTotal: 0,
                        status: 'idle',
                        errorMessage: null
                    });
            //
            if (settings.invokeDefaults) {
                $.fileupload._startedDefault(settings.uploaderId);
            }
            if (settings.onStarted != null) {
                settings.onStarted(settings.uploaderId);
            }
        });

        this.data('settings', settings);
    };

    $.fileupload = new FileUpload();

}) (jQuery);