# ============================================================================
# :core:database — consumer R8 rules
#
# Room generates its implementations at compile time, so DAO method names may be
# obfuscated safely. Two things must survive:
#   - entity field names, because column names are derived from them
#   - the generated *_Impl classes, which Room resolves via Class.forName()
# ============================================================================

-keep class az.pekstudios.pekseries.core.database.** {
    <init>(...);
    <fields>;
}

-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**
