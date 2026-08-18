# ============================================================================
# :core:model — consumer R8 rules
#
# Every class here is deserialised by Gson using *field-name matching*: there is
# not a single @SerializedName annotation in the module. R8 renames fields by
# default, after which Gson silently matches nothing and populates the objects
# with nulls and empty lists.
#
# The failure only appears in a minified build, never in debug, so these rules
# are the difference between a working release and one that shows empty screens.
# If you add @SerializedName to every field, these can be relaxed.
# ============================================================================

-keep class az.pekstudios.pekseries.core.model.** {
    <init>(...);
    <fields>;
    <methods>;
}

# Kotlin data classes with default parameter values get a synthetic constructor
# carrying a DefaultConstructorMarker; Gson picks it up via reflection.
-keepclassmembers class az.pekstudios.pekseries.core.model.** {
    synthetic <init>(...);
}
