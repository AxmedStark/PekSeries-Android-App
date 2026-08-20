package az.pekstudios.pekseries.core.domain.di

import javax.inject.Qualifier

/**
 * A CoroutineScope that lives as long as the process.
 *
 * For work that must outlive whatever screen started it — topic reconciliation
 * being the motivating case, since it has to run from a Service callback where
 * there is no viewModelScope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
