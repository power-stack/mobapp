package com.stackbase.mobapp.utils;

abstract public class Constant {
    /**
     * Whether to use autofocus by default.
     */
    public static final boolean DEFAULT_TOGGLE_AUTO_FOCUS = true;
    /**
     * Whether to initially disable continuous-picture and continuous-video focus modes.
     */
    public static final boolean DEFAULT_DISABLE_CONTINUOUS_FOCUS = true;
    /**
     * Whether the light should be initially activated by default.
     */
    public static final String DEFAULT_TOGGLE_LIGHT = "auto";
    /**
     * The default OCR engine to use.
     */
    public static final String DEFAULT_OCR_ENGINE_MODE = "Tesseract";
    /**
     * The default page segmentation mode to use.
     */
    public static final String DEFAULT_PAGE_SEGMENTATION_MODE = "Auto";
    /**
     * Whether to beep by default when the shutter button is pressed.
     */
    public static final boolean DEFAULT_TOGGLE_BEEP = false;
    /**
     * Whether to initially show a looping, real-time OCR display.
     */
    public static final boolean DEFAULT_TOGGLE_CONTINUOUS = false;
    /**
     * Whether to initially reverse the image returned by the camera.
     */
    public static final boolean DEFAULT_TOGGLE_REVERSED_IMAGE = false;
    /**
     * The default subdir to save pictures and data
     */
    public static final String DEFAULT_STORAGE_DIR = "esse/mobapp/";

    /**
     * The default subdir to save pictures and data
     */
    public static final String DEFAULT_MESSAGE_DIR = "messages";

    /**
     * Whether to send notification after upload finish
     */
    public static final boolean DEFAULT_MESSAGE_NOTIFY = true;

    /**
     * Whether vibrate when send notification.
     */
    public static final boolean DEFAULT_MESSAGE_VIBRATE = true;

    /**
     * The default url to download ocr packages
     */
    public static final String DEFAULT_DOWNLOAD_URL = "http://www.idevops.net:33080/files";

    /**
     * The default url to connect to Server API
     */
    public static final String DEFAULT_API_ENDPOINT = "http://192.168.1.231:8089";

    public static final String KEY_PREFERENCE_CATEGORY_OCR = "preference_category_ocr";
    public static final String KEY_SOURCE_LANGUAGE_PREFERENCE = "sourceLanguageCodeOcrPref";
    // Preference keys not carried over from ZXing project
    public static final String KEY_PAGE_SEGMENTATION_MODE = "preference_page_segmentation_mode";
    public static final String KEY_OCR_ENGINE_MODE = "preference_ocr_engine_mode";
    public static final String KEY_CHARACTER_BLACKLIST = "preference_character_blacklist";
    public static final String KEY_CHARACTER_WHITELIST = "preference_character_whitelist";
    public static final String KEY_TOGGLE_LIGHT = "preference_toggle_light";
    public static final String KEY_TRANSLATOR = "preference_translator";
    public static final String KEY_CONTINUOUS_PREVIEW = "preference_capture_continuous";

    public static final String KEY_AUTO_FOCUS = "preferences_auto_focus";
    public static final String KEY_DISABLE_CONTINUOUS_FOCUS = "preferences_disable_continuous_focus";
    public static final String KEY_HELP_VERSION_SHOWN = "preferences_help_version_shown";
    public static final String KEY_NOT_OUR_RESULTS_SHOWN = "preferences_not_our_results_shown";
    public static final String KEY_REVERSE_IMAGE = "preferences_reverse_image";
    public static final String KEY_PLAY_BEEP = "preferences_play_beep";
    public static final String KEY_VIBRATE = "preferences_vibrate";

    public static final String KEY_STORAGE_DIR = "preference_storage_dir";

    public static final String KEY_MESSAGE_NOTIFY = "preference_message_notify";
    public static final String KEY_MESSAGE_VIBRATE = "preference_message_vibrate";

    public static final String KEY_OCR_DOWNLOAD_URL = "preference_ocr_download_url";
    public static final String KEY_REMOTE_API_ENDPOINT = "preference_remote_api_endpoint";
    public static final String KEY_REMOTE_ACCESS_TOKEN = "preference_remote_access_token";

    public static final String INTENT_PACKAGE = "io.esse.mobapp";

    public static final String INTENT_KEY_ID = "INTENT_NAME_ID";
    public static final String INTENT_KEY_NAME = "INTENT_KEY_NAME";
    public static final String INTENT_KEY_PIC_FOLDER = "INTENT_KEY_PIC_FOLDER";
    public static final String INTENT_KEY_PIC_FULLNAME = "INTENT_KEY_PIC_FULLNAME";
    public static final String INTENT_KEY_ID_JSON_FILENAME = "INTENT_KEY_ID_JSON_FILENAME";
    public static final String INTENT_KEY_BORROWER_OBJ = "INTENT_KEY_BORROWER_OBJ";
    public static final String INTENT_KEY_PREFERENCES_TYPE = "INTENT_KEY_PREFERENCES_TYPE";

    public static final int MESSAGE_KEY_GUIUPDATEIDENTIFIER = 100;
    public static final String MESSAGE_KEY_PROGRESS_BAR_POSITION = "MESSAGE_KEY_PROGRESS_BAR_POSITION";
    public static final String MESSAGE_KEY_PROGRESS_BAR_PROGRESS = "MESSAGE_KEY_PROGRESS_BAR_PROGRESS";
    public static final String MESSAGE_KEY_PROGRESS_BORROWER_NAME = "MESSAGE_KEY_PROGRESS_BORROWER_NAME";
}
