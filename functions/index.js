const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();

exports.checkNewEpisodes = onSchedule("every 60 minutes", async (event) => {
    const now = new Date()
    const oneHourAgo = new Date(now.getTime() - 65 * 60 * 1000);

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

            if (airTime >= oneHourAgo && airTime <= now) {
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
                    }
                };

                await admin.messaging().send(message);
                notificationsSent++;
            }
        }

        console.log("Check success. Series: ${notificationsSent}");
    } catch (error) {
        console.error("Check error:", error);
    }
});