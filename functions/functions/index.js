const { onValueWritten } = require("firebase-functions/v2/database");
const { initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.notifyNewMeeting = onValueWritten(
  {
    ref: "/meetings/{meetingId}",
  },
  (event) => {
    if (!event.data.after.exists()) return null;
    if (event.data.before.exists()) return null;

    const meeting = event.data.after.val();
    const payload = {
      notification: {
        title: "Новое заседание кафедры",
        body: `Тема: ${meeting.topic}, Дата: ${meeting.date}, Время: ${meeting.time}`,
      },
    };

    console.log("Sending notification for new meeting:", meeting);
    return getMessaging()
      .sendToTopic("meetings", payload)
      .then((response) => {
        console.log("Successfully sent message:", response);
        return null;
      })
      .catch((error) => {
        console.error("Error sending message:", error);
        return null;
      });
  }
);

exports.notifyArchivedMeeting = onValueWritten(
  {
    ref: "/archive/{meetingId}",
  },
  (event) => {
    if (!event.data.after.exists()) return null;
    if (event.data.before.exists()) return null;

    const meeting = event.data.after.val();
    const payload = {
      notification: {
        title: "Заседание архивировано",
        body: `Тема: ${meeting.topic}, Дата: ${meeting.date}, Время: ${meeting.time}`,
      },
    };

    console.log("Sending notification for archived meeting:", meeting);
    return getMessaging()
      .sendToTopic("meetings", payload)
      .then((response) => {
        console.log("Successfully sent message:", response);
        return null;
      })
      .catch((error) => {
        console.error("Error sending message:", error);
        return null;
      });
  }
);