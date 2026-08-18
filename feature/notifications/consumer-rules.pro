# ============================================================================
# :feature:notifications — consumer R8 rules
#
# Room generates its implementations at compile time, so most of it survives
# minification on its own. Entities still need their field names because column
# names are derived from them, and the generated *_Impl classes are looked up
# reflectively by Room.databaseBuilder().
# ============================================================================

-keep class az.pekstudios.pekseries.feature.notifications.data.** {
    <init>(...);
    <fields>;
}

-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**
