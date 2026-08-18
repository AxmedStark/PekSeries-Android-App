const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();

const SCHEDULE_MINUTES = 60;

exports.checkNewEpisodes = onSchedule(`every ${SCHEDULE_MINUTES} minutes`, async (event) => {
    const now = new Date();
    // Must match the schedule exactly. A 65-minute lookback on a 60-minute
    // schedule overlaps by 5 minutes, so anything airing in that window was
    // notified twice.
    const windowStart = new Date(now.getTime() - SCHEDULE_MINUTES * 60 * 1000);

    try {
        console.log("Request TVMaze schedule...");

        const [tvRes, webRes] = await Promise.all([
            fetch("https://api.tvmaze.com/schedule"),
            fetch("https://api.tvmaze.com/schedule/web")
        ]);

        const tvEpisodes = await tvRes.json();
        const webEpisodes = await webRes.json();
        const allEpisodes = [...tvEpisodes, ...webEpisodes];

        let notificationsSent = 0;

        for (const ep of allEpisodes) {
            if (!ep.airstamp) continue;

            const airTime = new Date(ep.airstamp);

            if (airTime >= windowStart && airTime <= now) {
                const showId = ep.show.id;
                const showName = ep.show.name;
                const epString = `S${ep.season} E${ep.number} - ${ep.name}`;

                const hours = String(airTime.getHours()).padStart(2, '0');
                const minutes = String(airTime.getMinutes()).padStart(2, '0');
                const formattedTime = `${hours}:${minutes}`;

                const message = {
                    topic: `show_${showId}`,
                    data: {
                        title: `New episode: ${showName}`,
                        body: `${epString} at ${formattedTime}`,
                        showId: String(showId)
                    },
                    // Data-only messages are normal priority by default, which
                    // Doze defers - sometimes for hours. The app builds the
                    // notification itself, so delivery must be prompt.
                    android: {
                        priority: "high"
                    }
                };

                await admin.messaging().send(message);
                notificationsSent++;
            }
        }

        console.log(`Check complete. Notifications sent: ${notificationsSent}`);
    } catch (error) {
        console.error("Check error:", error);
    }
});