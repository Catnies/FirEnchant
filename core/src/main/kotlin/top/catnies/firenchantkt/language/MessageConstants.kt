package top.catnies.firenchantkt.language

// 消息常量
object MessageConstants {
    const val DATABASE_CONNECT_ERROR = "database_connect_error"
    const val DATABASE_TABLE_CREATE_ERROR = "database_table_create_error"

    const val COMMAND_RELOAD_SUCCESS = "command_reload_success"
    const val COMMAND_VERSION_SUCCESS = "command_version_success"
    const val COMMAND_CONSOLE_CANT_EXECUTE = "command_console_cant_execute"
    const val COMMAND_DEBUG_GET_LOCATION_BOOK_SHELF_COUNT_EXECUTE =
        "command_debug_get_location_book_shelf_count_execute"
    const val COMMAND_DEBUG_GET_PLAYER_ENCHANTMENT_SEED_EXECUTE = "command_debug_get_player_enchantment_seed_execute"
    const val COMMAND_BROKEN_GEAR_BREAK_MAIN_HAND_SUCCESS = "command_broken_gear_break_main_hand_success"
    const val COMMAND_BROKEN_GEAR_BREAK_MAIN_HAND_FAIL = "command_broken_gear_break_main_hand_fail"
    const val COMMAND_BROKEN_GEAR_FIX_MAIN_HAND_SUCCESS = "command_broken_gear_fix_main_hand_success"
    const val COMMAND_BROKEN_GEAR_FIX_MAIN_HAND_FAIL = "command_broken_gear_fix_main_hand_fail"
    const val COMMAND_GIVE_BOOK_ENCHANTMENT_NOT_FOUND = "command_give_book_enchantment_not_found"
    const val COMMAND_GIVE_BOOK_ENCHANTMENT_SUCCESS_RECEIVE = "command_give_book_enchantment_success_receive"
    const val COMMAND_GIVE_BOOK_ENCHANTMENT_SUCCESS_EXECUTE = "command_give_book_enchantment_success_execute"

    const val RESOURCE_ENCHANTMENT_FILE_ERROR = "resource_enchantment_file_error"
    const val RESOURCE_ENCHANTMENT_FILE_PROVIDER_NOT_FOUND = "resource_enchantment_file_provider_not_found"
    const val RESOURCE_ENCHANTMENT_FILE_ITEM_NOT_FOUND = "resource_enchantment_file_item_not_found"
    const val RESOURCE_ORIGINAL_BOOK_MISSING_KEY = "resource_original_book_missing_key"
    const val RESOURCE_ORIGINAL_BOOK_INVALID_ENCHANTMENT = "resource_original_book_invalid_enchantment"
    const val RESOURCE_HOOK_ITEM_PROVIDER_NOT_FOUND = "resource_hook_item_provider_not_found"
    const val RESOURCE_HOOK_ITEM_NOT_FOUND = "resource_hook_item_not_found"
    const val RESOURCE_MENU_STRUCTURE_ERROR = "resource_menu_structure_error"
    const val RESOURCE_VALUE_INVALID_ERROR = "resource_value_invalid_error"
    const val RESOURCE_VALUE_NOT_FOUND = "resource_value_not_found"

    const val PLUGIN_COMPATIBILITY_HOOK_SUCCESS = "plugin_compatibility_hook_success"
    const val PLUGIN_FUNCTION_NOT_ENABLED = "plugin_function_not_enabled"

    const val CONFIG_ACTION_TYPE_UNKNOWN = "config_action_type_unknown"
    const val CONFIG_CONDITION_TYPE_UNKNOWN = "config_condition_type_unknown"
    const val CONFIG_ACTION_INVALID_ARGS = "config_action_invalid_args"
    const val CONFIG_CONDITION_INVALID_ARGS = "config_condition_invalid_args"
    const val CONFIG_ACTION_RUNTIME_ARGS_CAST_FAIL = "config_action_runtime_args_cast_fail"
    const val CONFIG_CONDITION_RUNTIME_ARGS_CAST_FAIL = "config_condition_runtime_args_cast_fail"

    const val ANVIL_ENCHANTED_BOOK_USE_FAIL = "anvil_enchanted_book_use_fail"
    const val ANVIL_ENCHANTED_BOOK_USE_FAIL_BREAK = "anvil_enchanted_book_use_fail_break"
    const val ANVIL_ENCHANTED_BOOK_USE_PROTECT_FAIL = "anvil_enchanted_book_use_protect_fail"

    const val REPAIR_TABLE_REPAIR_ITEM_RECEIVE_SUCCESS = "repair_table_repair_item_receive_success"
    const val REPAIR_TABLE_REPAIR_ITEM_CANCEL_SUCCESS = "repair_table_repair_item_cancel_success"
    const val REPAIR_TABLE_REPAIR_ITEM_QUEUE_FULL = "repair_table_repair_item_queue_full"
}
