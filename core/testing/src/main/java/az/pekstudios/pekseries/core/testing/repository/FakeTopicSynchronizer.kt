package az.pekstudios.pekseries.core.testing.repository

import az.pekstudios.pekseries.core.domain.repository.TopicSynchronizer

class FakeTopicSynchronizer : TopicSynchronizer {
    var startCallCount = 0
    var reconcileCallCount = 0

    override fun start() {
        startCallCount++
    }

    override suspend fun reconcile() {
        reconcileCallCount++
    }
}
