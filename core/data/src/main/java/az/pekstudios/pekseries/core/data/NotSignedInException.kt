package az.pekstudios.pekseries.core.data

/**
 * Thrown when a per-user Firestore collection is accessed with no signed-in
 * user. Mapped to DataError.Unauthenticated so the UI can prompt for login
 * rather than rendering an empty watchlist.
 */
class NotSignedInException : IllegalStateException("No signed-in user")
