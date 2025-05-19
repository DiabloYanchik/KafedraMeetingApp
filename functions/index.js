const { onValueWritten } = require("firebase-functions/v2/database");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");
const { getDatabase } = require("firebase-admin/database");
const { logger } = require("firebase-functions");
const { onRequest } = require("firebase-functions/v2/https");

initializeApp();

exports.notifyNewMeeting = onValueWritten(
    {
        ref: "/meetings/{meetingId}",
        region: "us-central1",
        timeoutSeconds: 60,
        memory: "256MiB",
    },
    (event) => {
        if (!event.data.after.exists() || event.data.before.exists()) {
            logger.info(`Skipping notifyNewMeeting for meetingId: ${event.params.meetingId}`);
            return null;
        }

        const meeting = event.data.after.val();
        const topic = "meetings";
        const payload = {
            notification: {
                title: "Новое заседание кафедры",
                body: `Тема: ${meeting.topic || "Не указано"}, Дата: ${meeting.date || "Не указано"}, Время: ${meeting.time || "Не указано"}`,
            },
            data: {
                title: "Новое заседание кафедры",
                message: `Тема: ${meeting.topic || "Не указано"}, Дата: ${meeting.date || "Не указано"}, Время: ${meeting.time || "Не указано"}`,
                meetingId: event.params.meetingId,
            },
        };

        logger.info(`Sending notification for new meeting: ${event.params.meetingId}`, { meeting, payload });
        return getMessaging()
            .sendToTopic(topic, payload)
            .then((response) => {
                logger.info(`Successfully sent new meeting notification: ${response}`, { meetingId: event.params.meetingId });
                return null;
            })
            .catch((error) => {
                logger.error(`Error sending new meeting notification: ${error.message}`, { meetingId: event.params.meetingId, error: JSON.stringify(error) });
                return null;
            });
    }
);

exports.notifyArchivedMeeting = onValueWritten(
    {
        ref: "/archive/{meetingId}",
        region: "us-central1",
        timeoutSeconds: 60,
        memory: "256MiB",
    },
    (event) => {
        if (!event.data.after.exists() || event.data.before.exists()) {
            logger.info(`Skipping notifyArchivedMeeting for meetingId: ${event.params.meetingId}`);
            return null;
        }

        const meeting = event.data.after.val();
        const topic = "meetings";
        const payload = {
            notification: {
                title: "Заседание архивировано",
                body: `Тема: ${meeting.topic || "Не указано"}, Дата: ${meeting.date || "Не указано"}, Время: ${meeting.time || "Не указано"}`,
            },
            data: {
                title: "Заседание архивировано",
                message: `Тема: ${meeting.topic || "Не указано"}, Дата: ${meeting.date || "Не указано"}, Время: ${meeting.time || "Не указано"}`,
                meetingId: event.params.meetingId,
            },
        };

        logger.info(`Sending notification for archived meeting: ${event.params.meetingId}`, { meeting });
        return getMessaging()
            .sendToTopic(topic, payload)
            .then((response) => {
                logger.info(`Successfully sent archived meeting notification: ${response}`, { meetingId: event.params.meetingId });
                return null;
            })
            .catch((error) => {
                logger.error(`Error sending archived meeting notification: ${error.message}`, { meetingId: event.params.meetingId, error });
                return null;
            });
    }
);

exports.checkScheduledNotifications = onSchedule(
    {
        schedule: "every 5 minutes",
        region: "us-central1",
        timeoutSeconds: 120,
        memory: "512MiB",
    },
    async (context) => {
        const db = getDatabase();
        const notificationsRef = db.ref("notifications");
        const now = Date.now();

        try {
            logger.info("Checking scheduled notifications", { currentTime: now });
            const snapshot = await notificationsRef.once("value");
            logger.info("Notifications snapshot", { data: snapshot.val() });

            if (!snapshot.exists()) {
                logger.info("No scheduled notifications found");
                return null;
            }

            const promises = [];
            snapshot.forEach((meetingSnapshot) => {
                const meetingId = meetingSnapshot.key;
                logger.info(`Processing meeting: ${meetingId}`);
                meetingSnapshot.forEach((notificationSnapshot) => {
                    const triggerTime = parseInt(notificationSnapshot.key);
                    const message = notificationSnapshot.val();
                    logger.info(`Found notification: meetingId=${meetingId}, triggerTime=${triggerTime}, message=${message}`);

                    if (triggerTime <= now) {
                        const topic = "meetings";
                        const payload = {
                            notification: {
                                title: "Скоро заседание кафедры!",
                                body: message,
                            },
                            data: {
                                title: "Скоро заседание кафедры!",
                                message: message,
                                meetingId: meetingId,
                            },
                        };

                        logger.info(`Sending scheduled notification for meeting: ${meetingId}, triggerTime: ${triggerTime}`);
                        promises.push(
                            getMessaging()
                                .sendToTopic(topic, payload)
                                .then((response) => {
                                    logger.info(`Successfully sent scheduled notification: ${response}`, { meetingId, triggerTime });
                                    return notificationsRef.child(meetingId).child(notificationSnapshot.key).remove();
                                })
                                .catch((error) => {
                                    logger.error(`Error sending scheduled notification: ${error.message}`, { meetingId, triggerTime, error: JSON.stringify(error) });
                                })
                        );
                    } else {
                        logger.info(`Notification not due yet: triggerTime=${triggerTime}, currentTime=${now}`);
                    }
                });
            });

            await Promise.all(promises);
            logger.info("Finished processing scheduled notifications");
            return null;
        } catch (error) {
            logger.error(`Error checking scheduled notifications: ${error.message}`, { error: JSON.stringify(error) });
            return null;
        }
    }
);

exports.testFCM = onRequest(
    { region: "us-central1" },
    async (req, res) => {
        const payload = {
            notification: {
                title: "Тестовое уведомление",
                body: "Это тестовое уведомление из Cloud Functions",
            },
            data: {
                title: "Тестовое уведомление",
                message: "Это тестовое уведомление из Cloud Functions",
            },
        };

        try {
            const response = await getMessaging().sendToTopic("meetings", payload);
            logger.info("Successfully sent test notification", { response });
            res.status(200).send("Notification sent: " + response);
        } catch (error) {
            logger.error("Error sending test notification", { error: JSON.stringify(error) });
            res.status(500).send("Error: " + error.message);
        }
    }
);